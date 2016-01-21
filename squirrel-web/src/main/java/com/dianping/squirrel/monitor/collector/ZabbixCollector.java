package com.dianping.squirrel.monitor.collector;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.squirrel.common.util.JsonUtils;
import com.dianping.squirrel.monitor.data.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ZabbixCollector extends AbstractCollector {

    private Map<String,String> hostIdMap = new HashMap<String, String>();

    private final String ZABBIX_COLLECTOR_API = "http://z.dp/api_jsonrpc.php";

    private final String AUTH_USERNAME = "dp.wang";

    private final String AUTH_PASSWORD = "vivi520882";

    @Autowired
    private ServerService serverService;

    private Set<String> itemTokey = new HashSet<String>(){{
        add("system.cpu.load[,avg1]");
        add("net.if.in[eth0,bytes]");
        add("net.if.out[eth0,bytes]");
        add("icmppingloss[,40]");
        add("vm.memory.size[total]");
        add("vm.memory.size[used]");
        add("network.retransmission");
        add("system.cpu.util[,idle,avg5]");
    }};

    private String authToken;

    @Scheduled(cron = "10/30 * * * * *")
    public void scheduled(){
        if(isLeader() && isProductEnv()){
            for(Map.Entry<String,String> hostIdEntity : hostIdMap.entrySet()){
                Data data = collectData(hostIdEntity.getKey(),hostIdEntity.getValue());
                dataManager.addData(data);
            }
        }
    }

    private Data collectData(String address,String hostId){
        Data data = new Data();
        data.setType(Data.DataType.ZabbixStats);
        ServerStats stats;
        try {
            Server server = serverService.findByAddress(address);
            String params = getAllItemJson(authToken,hostId);
            String zbStatsStr = RequestUtil.sendPost(ZABBIX_COLLECTOR_API,params);
            ZabbixResult zabbixResult = JsonUtils.fromStr(zbStatsStr,ZabbixResult.class);
            Map<String,String> tempStats = new HashMap<String, String>();
            for(Map<String,String> item : zabbixResult.getResult()){
                if(itemTokey.contains(item.get("key_"))){
                    tempStats.put(item.get("key_"),item.get("lastvalue"));
                }
            }
            if(tempStats.isEmpty() || server == null){
                return null;
            }
            stats = translate(tempStats);
            stats.setServerId(server.getId());
            data.setStats(stats);
        } catch (Exception e) {
            return null;
        }
        return data;
    }

    private ServerStats translate(Map<String,String> stats){
        checkNotNull(stats,"stats is null");
        ServerStats serverStats = new ServerStats();
        serverStats.setCurr_time((int) (System.currentTimeMillis()/1000));
        serverStats.setIcmp_loss(Float.parseFloat(stats.get("icmppingloss[,40]")));
        serverStats.setMem_total(Long.parseLong(stats.get("vm.memory.size[total]")));
        serverStats.setMem_used(Long.parseLong(stats.get("vm.memory.size[used]")));
        serverStats.setNet_in(Double.parseDouble(stats.get("net.if.in[eth0,bytes]")));
        serverStats.setNet_out(Double.parseDouble(stats.get("net.if.out[eth0,bytes]")));
        serverStats.setProcess_load(Float.parseFloat(stats.get("system.cpu.load[,avg1]")));
        serverStats.setRetransmission(Integer.parseInt(stats.get("network.retransmission")));
        return serverStats;
    }


    @Scheduled(cron = "0 0 1 * * *")
    private void refreshBasicInfo(){
        authToken = null;
        hostIdMap.clear();
        try {
            String zbReply = RequestUtil.sendPost(ZABBIX_COLLECTOR_API,getAuthJson());
            AuthenResult authenResult = JsonUtils.fromStr(zbReply,AuthenResult.class);
            authToken = authenResult.getResult();
            List<Server> serverList = serverService.findAllMemcachedServers();
            Server s= new Server();
            s.setAddress("10.1.1.116:11211");
            serverList.add(s);
            for (Server server : serverList) {
                try {
                    String[] hostAndPort = server.getAddress().split(":");
                    String ip = hostAndPort[0];
                    zbReply = RequestUtil.sendPost(ZABBIX_COLLECTOR_API,getHostIdJson(ip));
                    ZabbixResult zabbixResult = JsonUtils.fromStr(zbReply, ZabbixResult.class);
                    List<Map<String, String>> items =zabbixResult.getResult();
                    if(items.size() < 1)
                        continue;
                    for (Map<String, String> item : items) {
                        if (item.containsKey("hostid")) {
                            hostIdMap.put(server.getAddress(), item.get("hostid"));
                        }
                    }
                } catch(Exception e1){

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String getAuthJson() throws IOException {
        RequestParams ar = new RequestParams();
        ar.setId(1);
        ar.setJsonrpc("2.0");
        ar.setMethod("user.login");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", AUTH_USERNAME);
        params.put("password", AUTH_PASSWORD);
        ar.setParams(params);
        return JsonUtils.toStr(ar);
    }

    public String getHostIdJson(String serverIp) throws IOException {
        RequestParams ar = new RequestParams();
        ar.setId(1);
        ar.setJsonrpc("2.0");
        ar.setMethod("host.get");
        ar.setAuth(authToken);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("output", "hostid");
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("ip", serverIp);
        params.put("filter", filter);
        ar.setParams(params);
        return JsonUtils.toStr(ar);
    }

    private String getAllItemJson(String auth, String host) throws IOException {
        RequestParams ar = new RequestParams();
        ar.setId(1);
        ar.setJsonrpc("2.0");
        ar.setMethod("item.get");
        ar.setAuth(auth);
        Map<String, Object> params = new HashMap<String, Object>();
        String[] stra = new String[2];
        stra[0] = "key_";
        stra[1] = "lastvalue";
        params.put("output", stra);
        params.put("hostids", host);
        ar.setParams(params);
        return JsonUtils.toStr(ar);
    }

    public class RequestParams{

        private String jsonrpc;
        private String method;
        private int id;
        private String auth;
        private Map<String,Object> params;

        public String getJsonrpc() {
            return jsonrpc;
        }

        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }
    }

    static class AuthenResult{
        private String jsonrpc;
        private String result;
        private int id;
        public String getJsonrpc() {
            return jsonrpc;
        }
        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }
        public String getResult() {
            return result;
        }
        public void setResult(String result) {
            this.result = result;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
    }

    static class ZabbixResult{
        private int id;
        private String jsonrpc;
        private List<Map<String,String>> result;
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getJsonrpc() {
            return jsonrpc;
        }
        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }
        public List<Map<String, String>> getResult() {
            return result;
        }
        public void setResult(List<Map<String, String>> result) {
            this.result = result;
        }
    }

}
