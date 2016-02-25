package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.scale.cluster.Server;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.*;

@JsonIgnoreProperties({"slotList","redisCluster"})
public class RedisServer extends Server {

    private String id;

    private Set<String> flags = new HashSet<String>();

    private String slotString;

    private List<Integer> slotList = new ArrayList<Integer>();

    private int slotSize;

    private String masterId;

    private RedisInfo info;

    private boolean migrating;

    private boolean importing;

    private int openSlot;

    private RedisCluster redisCluster;

    public RedisServer(String address) {
        super(address);
    }

    public RedisServer(String ip, int port) {
        super(ip, port);
    }

    public void setFlags(String flagStr) {
        flags.clear();
        for (String flag : flagStr.split(",")) {
            flags.add(flag);
        }
    }

    public boolean isMyself() {
        return flags.contains("myself");
    }

    public boolean isMaster() {
        return flags.contains("master");
    }

    public boolean isSlave() {
        return flags.contains("slave");
    }

    public boolean isPartialFail() {
        return flags.contains("fail?");
    }

    public boolean isFail() {
        return flags.contains("fail");
    }

    public boolean isHandShake() {
        return flags.contains("handshake");
    }

    public boolean isNoAddr() {
        return flags.contains("noaddr");
    }

    public boolean isNoFlags() {
        return flags.contains("noflags");
    }

    public boolean isAlive() {
        return !isPartialFail() && !isFail() && !isHandShake() && !isNoAddr()
                && !isNoFlags();
    }


    private Map<String, String> parseServerConfig(List<String> config) {
        String quota = "\"";
        Map<String, String> result = new HashMap<String, String>();
        if (config == null)
            return result;
        for (int i = 0; i < config.size(); i += 2) {
            config.set(i + 1, config.get(i + 1).replaceAll(quota, ""));
            result.put(config.get(i), config.get(i + 1));
        }
        return result;
    }

    private Map<String, String> parseServerInfo(String infoString) {
        Map<String, String> data = new HashMap<String, String>();
        String[] infoArray = infoString.split("\r\n");
        for (String info : infoArray) {
            info.trim();
            String[] each = info.split(":");
            if (each.length > 1)
                data.put(each[0], each[1]);
        }
        return data;
    }

    public void setSlots(String[] slots) {
        this.slotString = null;
        if (slots != null) {
            for (String segment : slots) {
                if (this.slotString == null) {
                    this.slotString = segment;
                } else {
                    this.slotString += ("," + segment);
                }
            }
        }
        slotList = updateSlotList(slotString);
        slotSize = slotList.size();
    }

    List<Integer> updateSlotList(String slotString) {
        if (slotString == null)
            return new ArrayList<Integer>();
        String[] segments = slotString.split(",");
        List<Integer> slotList = new ArrayList<Integer>();
        for (String segment : segments) {
            segment = segment.trim();
            if (StringUtils.isEmpty(segment))
                continue;
            int idx = segment.indexOf('-');
            if (idx == -1) {
                slotList.add(Integer.parseInt(segment));
            } else if (segment.startsWith("[")) { // 正在传输
                this.migrating = true;
            } else {
                int end = Integer.parseInt(segment.substring(idx + 1).trim());
                int start = Integer.parseInt(segment.substring(0, idx).trim());
                if (end < start) {
                    start = start ^ end;
                    end = start ^ end;
                    start = start ^ end;
                }
                for (int i = start; i <= end; i++) {
                    slotList.add(i);
                }

            }
        }
        // deduplicate
        Collections.sort(slotList);
        List<Integer> newList = new ArrayList<Integer>();
        if (slotList.size() > 0) {
            newList.add(slotList.get(0));
        }
        for (int i = 1; i < slotList.size(); i++) {
            int n = slotList.get(i);
            if (n != slotList.get(i - 1)) {
                newList.add(n);
            }
        }
        return newList;
    }

    String updateSlotString(List<Integer> slotList) {
        Collections.sort(slotList);
        StringBuilder ss = new StringBuilder();
        int i = 0;
        while (i < slotList.size()) {
            int j = i;
            for (; j < slotList.size() - 1
                    && (slotList.get(j + 1) == slotList.get(j) + 1 || slotList
                    .get(j + 1).equals(slotList.get(j))); j++) {
            }
            if (j == i || slotList.get(i).equals(slotList.get(j))) {
                ss.append(slotList.get(i)).append(',');
            } else {
                ss.append(slotList.get(i)).append('-').append(slotList.get(j))
                        .append(',');
            }
            i = j + 1;
        }
        return ss.length() > 0 ? ss.substring(0, ss.length() - 1) : "";
    }

    public RedisInfo loadRedisInfo() {
        Jedis jedis = new Jedis(getIp(), getPort());
        String password = getRedisCluster().getPassword();
        info = new RedisInfo();
        try {
            if(password != null && !"".equals(password)){
                jedis.auth(password);
            }
            List<String> redisConfig = jedis.configGet("*");
            String redisInfo = jedis.info();
            Map<String, String> redisConfigMap = parseServerConfig(redisConfig);
            Map<String, String> redisInfoMap = parseServerInfo(redisInfo);

            info.setMaxMemory(Long.parseLong(redisConfigMap.get("maxmemory")) / 1024 / 1024);
            info.setUsedMemory(Long.parseLong(redisInfoMap.get("used_memory")) / 1024 / 1024);
            info.calculateUsed();
            info.setTotal_connections(Integer.parseInt(redisInfoMap.get("total_connections_received")));
            info.setConnected_clients(Integer.parseInt(redisInfoMap.get("connected_clients")));
            info.setQps(Integer.parseInt(redisInfoMap.get("instantaneous_ops_per_sec")));
            info.setInput_kbps(Double.parseDouble(redisInfoMap.get("instantaneous_input_kbps")));
            info.setOutput_kbps(Double.parseDouble(redisInfoMap.get("instantaneous_output_kbps")));
            info.setUsed_cpu_sys(Double.parseDouble(redisInfoMap.get("used_cpu_sys")));
            info.setUsed_cpu_sys_children(Double.parseDouble(redisInfoMap.get("used_cpu_sys_children")));
            info.setUsed_cpu_user(Double.parseDouble(redisInfoMap.get("used_cpu_user_children")));
            info.setUsed_cpu_user_children(Double.parseDouble(redisInfoMap.get("used_cpu_user_children")));
            //this.migrating = redisInfoMap.get("migrate_cached_sockets").equals("1");

            String nodesStr = jedis.clusterNodes();
            String[] nodes = nodesStr.split("\n");
            for(String node : nodes){
                if(node.contains("myself")){
                    if(node.contains("<")){
                        importing = true;
                        String open = node.substring(node.indexOf("[")+1,node.indexOf("-<-"));
                        openSlot = Integer.parseInt(open);
                    }else if(node.contains(">")){
                        migrating = true;
                        String open = node.substring(node.indexOf("[")+1,node.indexOf("->-"));
                        openSlot = Integer.parseInt(open);
                    }
                }
            }

        } catch (Exception e) {

        } finally {
            jedis.close();
        }
        return info;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public String getSlotString() {
        return slotString;
    }

    public void setSlotString(String slotString) {
        this.slotString = slotString;
    }

    public List<Integer> getSlotList() {
        return slotList;
    }

    public void setSlotList(List<Integer> slotList) {
        this.slotList = slotList;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public RedisInfo getInfo() {
        return info;
    }

    public void setInfo(RedisInfo info) {
        this.info = info;
    }

    public int getSlotSize() {
        return slotSize;
    }

    public void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    public boolean getMigrating() {
        return migrating;
    }

    public void setMigrating(boolean migrating) {
        this.migrating = migrating;
    }

    public boolean getImporting() {
        return importing;
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    public int getOpenSlot() {
        return openSlot;
    }

    public void setOpenSlot(int openSlot) {
        this.openSlot = openSlot;
    }

    public RedisCluster getRedisCluster() {
        return redisCluster;
    }

    public void setRedisCluster(RedisCluster redisCluster) {
        this.redisCluster = redisCluster;
    }
}
