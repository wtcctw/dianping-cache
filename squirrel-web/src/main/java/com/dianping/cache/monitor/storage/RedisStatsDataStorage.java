package com.dianping.cache.monitor.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.impl.RedisConnectionFactory;
import com.dianping.cache.service.RedisStatsService;
import com.dianping.cache.service.ServerService;
import com.dianping.combiz.spring.context.SpringLocator;

public class RedisStatsDataStorage extends AbstractStatsDataStorage{
	
	private static final int DEFAULT_PORT = 11211;

	private ServerService serverService;
	
	private RedisStatsService redisStatsService;
	
	private ExecutorService pool;

	
	public RedisStatsDataStorage(){
		logger.info("start to store Redis stats .");
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				storage();
			}
			
		}, 5, getStoragerInterval(), TimeUnit.SECONDS);
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
		List<Server> serverList = serverService.findAllRedisServers();
		for(Server server : serverList){
			pool.submit(new InsertData(server));
		}
	}
	private class InsertData implements Runnable{
	    	private Server server;
	    	
	    	public InsertData(){
	    	}
	    	public InsertData(Server server){
	    		this.server = server;
	    	}
			@Override
			public void run() {
				try {
					Jedis jedis = RedisConnectionFactory.getConnection(server.getAddress());
					Map<String,String> data = parseRedisInfo(jedis.info());
					
					RedisStats stat = processStats(data);
					stat.setServerId(server.getId());
					//stat.setMemory_used(data.get("used_memory"));
					redisStatsService.insert(stat);
					 
				}catch (Exception e){
					logger.error("Stored"+server.getAddress()+" Redis stats encountered Exception : "+e);
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
		stat.setMemory_used(Integer.parseInt(data.get("used_memory"))/1024/1024);
		stat.setTotal_commands(Integer.parseInt(data.get("total_commands_processed")));
		stat.setTotal_connections(Integer.parseInt(data.get("total_connections_received")));
		return stat;
	}
	
}
