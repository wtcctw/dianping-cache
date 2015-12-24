package com.dianping.cache.monitor.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.service.ServerStatsService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.combiz.spring.context.SpringLocator;

public class ServerStatsDataStorage extends AbstractStatsDataStorage {
	
	public static boolean START_SS = true;

	private ServerService serverService;
	
	private ServerStatsService serverStatsService;
	
	private ExecutorService pool;
	
	private static final String ZBURL = "http://z.dp/api_jsonrpc.php";
	
	private ZabbixInfo zbInfo = null;

	public static boolean REFRESH = true;

	public ServerStatsDataStorage() {
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				storage();
			}
			
		}, getStoragerInterval(), getStoragerInterval(), TimeUnit.SECONDS);
	}

	private void storage() {
		
		if(!START_SS || !isMaster()){
			return;
		}
		logger.info("Start storage work ^_^");
		if (REFRESH || zbInfo == null) {
			this.refreshZabbixInfo();
			REFRESH = false;
		}
		Map<String, String> hostids = zbInfo.getServerToHost();
		Set<String> itemKey = zbInfo.getItemTokey();
		ObjectMapper objectMapper = new ObjectMapper();
		for (Map.Entry<String, String> host : hostids.entrySet()) {
			logger.info("submit store job to thread");
			pool.submit(new InsertData(host));
		}

	}

	public static void main(String[] s) {
		System.out.println(Float.MAX_VALUE);
		System.out.println(Float.MIN_VALUE);
		new ServerStatsDataStorage().storage();

	}
	protected void init(){
		setPool(Executors.newFixedThreadPool(5));
		serverService = SpringLocator.getBean("serverService");
		serverStatsService = SpringLocator.getBean("serverStatsService");
	}
	private void refreshZabbixInfo() {
		logger.info("Refresh Zabbix Info !");
		ZabbixInfo zb = this.getZbInfo();
		String result = RequestUtil.sendPost(ZBURL,this.getAuthJson());
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			AuthenResult ar = objectMapper.readValue(result, AuthenResult.class);
			zb.setAuthToken(ar.getResult());
		} catch (Exception e) {
			logger.error("Convert AuthenResult from Zabbix response error ! "+e);
		} 
		List<Server> serverList = serverService.findAllMemcachedServers();
		for (Server server : serverList) {
			try {
				ServerInfo serverInfo = parseServer(server.getAddress());
				result = RequestUtil.sendPost(ZBURL,this.getHostJson(serverInfo.ip));
				Result hr = objectMapper.readValue(result, Result.class);
				List<Map<String, String>> items =hr.getResult();
				if(items.size() < 1)
					continue;
				for (Map<String, String> item : items) {
					if (item.containsKey("hostid")) {
						logger.info("Collect hostid" + item.get("hostid") + "for server address" + server.getAddress());
						zb.getServerToHost().put(server.getAddress(), item.get("hostid"));
					}
				}
			} catch (RuntimeException e) {
				logger.error("Convert HostResult from Zabbix response error !"+e);
			} catch(Exception e1){
				logger.error("Convert HostResult from Zabbix response error !"+e1);
			}
		}

	}

	private ServerStats processStats(Map<String,String> stats){
		 int curr_time = (int) (System.currentTimeMillis()/1000);
		 float process_load = Float.parseFloat(stats.get("system.cpu.load[,avg1]"));
		 double net_in = Double.parseDouble(stats.get("net.if.in[eth0,bytes]"));
		 double net_out = Double.parseDouble(stats.get("net.if.out[eth0,bytes]"));
		 long mem_total = Long.parseLong(stats.get("vm.memory.size[total]"));
		 long mem_used = Long.parseLong(stats.get("vm.memory.size[used]"));
		 float icmp_loss = Float.parseFloat(stats.get("icmppingloss[,40]"));
		 int retransmission = Integer.parseInt(stats.get("network.retransmission"));
		
		 ServerStats serverStats = new ServerStats();
		 serverStats.setCurr_time(curr_time);
		 serverStats.setIcmp_loss(icmp_loss);
		 serverStats.setMem_total(mem_total);
		 serverStats.setMem_used(mem_used);
		 serverStats.setNet_in(net_in);
		 serverStats.setNet_out(net_out);
		 serverStats.setProcess_load(process_load);
		 serverStats.setRetransmission(retransmission);
		 return serverStats;
	}
	
	
	public String getAuthJson() {

		JSON json = new JSON();
		Map<String, Object> authMap = new HashMap<String, Object>();
		authMap.put("jsonrpc", "2.0");
		authMap.put("method", "user.login");
		authMap.put("id", 1);

		Map<String, String> params = new HashMap<String, String>();
		params.put("user", "dp.wang");
		params.put("password", "vivi520882");

		authMap.put("params", params);

		StringBuffer sb = new StringBuffer();
		json.appendMap(sb, authMap);

		return sb.toString();
	}

	public String getHostJson(String serverIp) {

		JSON json = new JSON();
		Map<String, Object> authMap = new HashMap<String, Object>();
		authMap.put("jsonrpc", "2.0");
		authMap.put("method", "host.get");
		authMap.put("id", 1);
		authMap.put("auth", this.getZbInfo().getAuthToken());
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("output", "hostid");
		Map<String, String> filter = new HashMap<String, String>();
		filter.put("ip", serverIp);
		params.put("filter", filter);
		authMap.put("params", params);
		StringBuffer sb = new StringBuffer();
		json.appendMap(sb, authMap);

		return sb.toString();
	}

	private static String getAllItemJson(String auth, String host) {
		JSON json = new JSON();
		Map<String, Object> authMap = new HashMap<String, Object>();
		authMap.put("jsonrpc", "2.0");
		authMap.put("method", "item.get");
		authMap.put("id", 1);
		authMap.put("auth", auth);

		Map<String, Object> params = new HashMap<String, Object>();
		String[] stra = new String[2];
		stra[0] = "key_";
		stra[1] = "lastvalue";
		params.put("output", stra);
		params.put("hostids", host);
		authMap.put("params", params);
		StringBuffer sb = new StringBuffer();
		json.appendMap(sb, authMap);

		return sb.toString();
	}

	public class ZabbixInfo {

		private String authToken;

		private Map<String, String> serverToHost = new HashMap<String, String>();

		private Set<String> itemTokey = new HashSet<String>();

		public void init() {
			itemTokey.add("system.cpu.load[,avg1]");
			itemTokey.add("net.if.in[eth0,bytes]");
			itemTokey.add("net.if.out[eth0,bytes]");
			itemTokey.add("icmppingloss[,40]");
			itemTokey.add("vm.memory.size[total]");
			itemTokey.add("vm.memory.size[used]");
			itemTokey.add("network.retransmission");
			itemTokey.add("system.cpu.util[,idle,avg5]");
		}

		public ZabbixInfo() {
			init();
		}

		public String getAuthToken() {
			return authToken;
		}

		public void setAuthToken(String authToken) {
			this.authToken = authToken;
		}

		public Map<String, String> getServerToHost() {
			return serverToHost;
		}

		public void setServerToHost(Map<String, String> serverToHost) {
			this.serverToHost = serverToHost;
		}

		public Set<String> getItemTokey() {
			return itemTokey;
		}

		public void setItemTokey(Set<String> itemTokey) {
			this.itemTokey = itemTokey;
		}
	}


	public ZabbixInfo getZbInfo() {
		if (zbInfo == null)
			zbInfo = new ZabbixInfo();
		return zbInfo;
	}

	public void setZbInfo(ZabbixInfo zbInfo) {
		this.zbInfo = zbInfo;
	}

	private static ServerInfo parseServer(String server) {
		ServerInfo si = new ServerInfo();
		int idx = server.indexOf(':');
		if (idx == -1) {
			si.ip = server;
			si.port = 11211;
		} else {
			si.ip = server.substring(0, idx);
			si.port = Integer.parseInt(server.substring(idx + 1));
		}
		return si;
	}

	public ExecutorService getPool() {
		return pool;
	}

	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}

	static class ServerInfo {
		String ip;
		int port;
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
	
	static class Result{
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
	
	private class InsertData implements Runnable{
		private Map.Entry<String, String> host;
		private ObjectMapper objectMapper;
		private Set<String> itemKey ;
		
		public InsertData(){}
		
		public InsertData(Map.Entry<String, String> host){
			this.host = host;
			itemKey = zbInfo.getItemTokey();
			objectMapper = new ObjectMapper();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			logger.info("Start to collect data");
			String server = host.getKey();
			String hostid = host.getValue();
			String result = RequestUtil.sendPost(ZBURL,getAllItemJson(zbInfo.getAuthToken(), hostid));

			try {
				Result ir = objectMapper.readValue(result, Result.class);
				List<Map<String, String>> items = ir.getResult();
				Map<String, String> stats = new HashMap<String, String>();
				for (Map<String, String> item : items) {
					if (itemKey.contains(item.get("key_"))) {
						stats.put(item.get("key_").toString(),
								item.get("lastvalue"));
					}
				}
				if(stats.isEmpty()){
					return;
				}
				
				ServerStats serverStats = processStats(stats);
				serverStats.setServerId(serverService.findByAddress(server).getId());
				logger.info("Insert data : id = " + serverStats.getId() + ";serverId = " +serverStats.getServerId());
				serverStatsService.insert(serverStats);

			} catch (Exception e) {
				logger.error("Convert ItemResult from Zabbix response error !" + e);
			} 
		}
		
	}
}
