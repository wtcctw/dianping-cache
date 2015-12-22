package com.dianping.cache.monitor.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.dianping.cache.scale1.cluster.redis.*;
import redis.clients.jedis.Jedis;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.impl.RedisConnectionFactory;
import com.dianping.cache.service.RedisStatsService;
import com.dianping.cache.service.ServerService;
import com.dianping.combiz.spring.context.SpringLocator;

public class RedisStatsDataStorage extends AbstractStatsDataStorage{
	
	private ServerService serverService;
	
	private RedisStatsService redisStatsService;
	
	private ExecutorService pool;

	
	public RedisStatsDataStorage(){
		logger.info("start to store Redis stats .  ");
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				storage();
			}
			
		}, 35, getStoragerInterval(), TimeUnit.SECONDS);
	}
	
	protected void init(){
		pool = Executors.newFixedThreadPool(5);
		serverService = SpringLocator.getBean("serverService");
		redisStatsService = SpringLocator.getBean("redisStatsService");
	}
	
	private void storage(){
		if(!isMaster()){
			return;
		}
		for(Map.Entry<String,RedisCluster> value : RedisManager.getClusterCache().entrySet()){
			pool.submit(new InsertData(value.getValue()));
		}
//		List<Server> serverList = serverService.findAllRedisServers();
//		for(Server server : serverList){
//			pool.submit(new InsertData(server));
//		}
	}
	private class InsertData implements Runnable{
	    	private Server server;

	    	private RedisCluster cluster;

	    	public InsertData(Server server){
	    		this.server = server;
	    	}

			public InsertData(RedisCluster cluster){
				this.cluster = cluster;
			}
			@Override
			public void run() {
				try {

					if (cluster == null) {
						Jedis jedis = RedisConnectionFactory.getConnection(server.getAddress());
						Map<String,String> data = parseRedisInfo(jedis.info());

						RedisStats stat = processStats(data);
						stat.setServerId(server.getId());
						//stat.setMemory_used(data.get("used_memory"));
						redisStatsService.insert(stat);
					} else {
						for(RedisNode node : cluster.getNodes()){
							RedisServer master = node.getMaster();
							RedisServer slave = node.getSlave();
							if(master.getInfo() != null){
								Server sm = serverService.findByAddress(master.getAddress());
								if(sm != null){
									RedisStats stat = processStats(master.getInfo());
									stat.setServerId(sm.getId());
									redisStatsService.insert(stat);
								}
							}
//							if(slave != null  && slave.getInfo() != null){
//								Server ss = serverService.findByAddress(slave.getAddress());
//								if (ss != null) {
//									RedisStats stat = processStats(slave.getInfo());
//									stat.setServerId(ss.getId());
//									redisStatsService.insert(stat);
//								}
//							}
						}
					}

				}catch (Exception e){
					logger.error("Stored Redis stats encountered Exception : "+e);
				}
			}
	    }
	
	private Map<String,String> parseRedisInfo(String infoString){
		Map<String,String> data = new HashMap<String,String>();
		String[] infoArray = infoString.split("\r\n");
		for(String info : infoArray){
			info.trim();
			String[] each = info.split(":");
			if(each.length > 1)
				data.put(each[0], each[1]);
		}
		return data;
	}
	
	
	private RedisStats processStats(Map<String,String> data){
		RedisStats stat = new RedisStats();
		stat.setCurr_time(System.currentTimeMillis()/1000);
		stat.setMemory_used(Long.parseLong(data.get("used_memory"))/1024/1024);
		stat.setTotal_connections(Integer.parseInt(data.get("total_connections_received")));
		stat.setConnected_clients(Integer.parseInt(data.get("connected_clients")));
		stat.setQps(Integer.parseInt(data.get("instantaneous_ops_per_sec")));
		stat.setInput_kbps(Double.parseDouble(data.get("instantaneous_input_kbps")));
		stat.setOutput_kbps(Double.parseDouble(data.get("instantaneous_output_kbps")));
		stat.setUsed_cpu_sys(Double.parseDouble(data.get("used_cpu_sys")));
		stat.setUsed_cpu_sys_children(Double.parseDouble(data.get("used_cpu_sys_children")));
		stat.setUsed_cpu_user(Double.parseDouble(data.get("used_cpu_user_children")));
		stat.setUsed_cpu_user_children(Double.parseDouble(data.get("used_cpu_user_children")));
		return stat;
	}

	private RedisStats processStats(RedisInfo info){
		RedisStats stat = new RedisStats();
		stat.setCurr_time(System.currentTimeMillis()/1000);
		stat.setMemory_used(info.getUsedMemory());
		stat.setTotal_connections(info.getTotal_connections());
		stat.setConnected_clients(info.getConnected_clients());
		stat.setInput_kbps(info.getInput_kbps());
		stat.setOutput_kbps(info.getOutput_kbps());
		stat.setQps(info.getQps());
		stat.setUsed_cpu_sys(info.getUsed_cpu_sys());
		stat.setUsed_cpu_sys_children(info.getUsed_cpu_sys_children());
		stat.setUsed_cpu_user(info.getUsed_cpu_user());
		stat.setUsed_cpu_user_children(info.getUsed_cpu_user_children());
		return stat;
	}
	
}
