package com.dianping.cache.scale.cluster.redis;

import java.util.ArrayList;
import java.util.List;


import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.instance.AppId;
import com.dianping.cache.scale.instance.Apply;
import com.dianping.cache.scale.instance.Result;
import com.dianping.cache.scale.instance.docker.DockerApply;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.util.ParseServersUtil;
import com.dianping.cache.util.SpringLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisScaler {


	private static Logger logger = LoggerFactory.getLogger(RedisScaler.class);
	private static final int RETRY = 5;
	private static Apply autoApply = new DockerApply();
	private static ServerService serverService;
	private static CacheConfigurationService cacheConfigurationService;
	private static AppId DEFAULT_APPID = AppId.redis10;
	static{
		serverService = SpringLocator.getBean("serverService");
		cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
	}
	public static void addMaster(final String cluster){
		logger.info("Cluster {} start addMaster operation",cluster);
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String servers = config.getServers();
		List<String> serverlist = ParseServersUtil.parseRedisServers(servers);
		Server server = serverService.findByAddress(serverlist.get(0));
		AppId appId = AppId.valueOf(server.getAppId());
		int operateid = autoApply.apply(appId, 1);
		Result result = autoApply.getValue(operateid);
		while (result.getStatus() == 100) {
			try {
				Thread.sleep(100);
				result = autoApply.getValue(operateid);
			} catch (InterruptedException e) {
				// if node is already scaled , it has type -2 ,need manual detroy it
				throw new ScaleException(e.toString());
			}
		}
		
		if (result.getStatus() == 200 && result.getInstances().size() == 1) {
			String master = result.getInstances().get(0).getIp() + ":"
					+ result.getAppId().getPort();
			RedisManager.addMaster(cluster, master);
			afterJoinCLuster(cluster, master);
		}
	}

	public static void addSlave(final String cluster, final String masterAddress) {
		logger.info("Cluster {} start addSlave operation,master Ip : {}",cluster,masterAddress);
		Server server = serverService.findByAddress(masterAddress);
		AppId appId;
		if(server != null  && server.getAppId() != null){
			appId = AppId.valueOf(server.getAppId());
		}else{
			appId = DEFAULT_APPID;
		}
		Result slave = null;
		List<Result> tmp = new ArrayList<Result>();
		for (int i = 0; i < RETRY ;i++){
			int operateid = autoApply.apply(appId, 1);
			Result result = autoApply.getValue(operateid);
			while (result.getStatus() == 100) {
				try {
					Thread.sleep(100);
					result = autoApply.getValue(operateid);
				} catch (InterruptedException e) {
					throw new ScaleException(e.toString());
				}
			}
			if(result.getStatus() == 200){
				tmp.add(result);
				slave = loadSlave(server,tmp);
				if(slave != null){
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						logger.error("wait for redis process  start error!",e);
						throw new ScaleException(e.toString());
					}
					String slaveAddress = slave.getInstances().get(0).getIp() + ":" + slave.getAppId().getPort();
					RedisManager.addSlaveToMaster(masterAddress, slaveAddress);
					afterJoinCLuster(cluster, slaveAddress);
					logger.info("Scale success , instance ip :{}",slaveAddress);
					break;
				}
			}
		}
		for(Result r : tmp){
			logger.info("Try to destroy instance {} which unnecessary .",r.getInstances());
			autoApply.destroy(r);
		}
	}


	public static void removeSlave(String cluster, String address) {
		beforeRemove(cluster,address);
		RedisManager.removeServer(cluster,address);
		afterRemove(cluster,address);
	}

	private static void beforeRemove(String cluster, String address){

	}

	private static void afterRemove(String cluster,String address) {
		Server server = serverService.findByAddress(address);
		if(server == null)
			return;
		deleteServerInCluster(cluster,address);// delete server from cluster config and server_cluster table  before in servers table
		String instanceId = server.getInstanceId();
		if(null != instanceId  &&  !"".equals(instanceId)){
			// container instance
			String appId = server.getAppId();
			Apply apply = new DockerApply();
			apply.destroy(appId,instanceId);
		}else{
			// none container
			serverService.delete(address);
		}
	}
	
	private static void afterJoinCLuster(String cluster, String address) {
		Server server = serverService.findByAddress(address);
		server.setType(1);
		serverService.update(server);
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String servers = config.getServers();
		address = address.trim();
		if(cluster.contains("redis")){
			config.setAddTime(System.currentTimeMillis());
			List<String> serverlist = ParseServersUtil.parseRedisServers(servers);
			String urlHead = "redis-cluster://";
			String urlTail = servers.substring(servers.indexOf("?"));
			serverlist.add(address);
			String newServers = "";
			for(String str : serverlist){
				newServers = newServers + str + ",";
			}
			newServers = newServers.substring(0,newServers.length()-1);//delete last ","
			newServers = urlHead + newServers + urlTail;
			config.setServers(newServers);
			cacheConfigurationService.update(config);
		}
	}
	
	private static void deleteServerInCluster(String cluster,String server){
		deleteServerFromConfig(cluster, server);
		//ServerClusterService serverClusterService = SpringLocator.getBean("serverClusterService");
		//serverClusterService.delete(server,cluster);//delete all servercluster which contains server
	}

	private static void deleteServerFromConfig(String cluster, String server) {
		//CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService"); 
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String servers = config.getServers();
		server = server.trim();
		if(cluster.contains("redis")){
			config.setAddTime(System.currentTimeMillis());
			List<String> serverlist = ParseServersUtil.parseRedisServers(servers);
			String urlHead = "redis-cluster://";
			String urlTail = servers.substring(servers.indexOf("?"));
			serverlist.remove(server);
			String newServers = "";
			for(String str : serverlist){
				newServers = newServers + str + ",";
			}
			newServers = newServers.substring(0,newServers.length()-1);//delete last ";"
			newServers = urlHead + newServers + urlTail;
			config.setServers(newServers);
			cacheConfigurationService.update(config);
		}else if(cluster.contains("memcached")){ // just see see
			config.setAddTime(System.currentTimeMillis());
			List<String> serverList = new ArrayList<String>(config.getServerList());
			serverList.remove(server);
			config.setServerList(serverList);
			config.setAddTime(System.currentTimeMillis());
			cacheConfigurationService.update(config);
		}
	}
	
	private static Result loadSlave(Server master,List<Result> tempResult){
		if(master == null || master.getHostIp() == null){
			return tempResult.remove(0);
		}
		for(int i = 0; i < tempResult.size(); i++){
			if(!	(tempResult.get(i).getInstances().get(0).getAgentIp())	.	equals	(master.getHostIp())){
				Result slave = tempResult.get(i);
				tempResult.remove(i);
				return slave;
			}
		}
		return null;
	}
	
	private static void loadMaster(){
		
	}
	
	private static void loadNode(){
		
	}

}
