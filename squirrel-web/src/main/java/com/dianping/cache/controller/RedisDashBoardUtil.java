package com.dianping.cache.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.scale.impl.RedisCluster;
import com.dianping.cache.scale.impl.RedisConnectionFactory;
import com.dianping.cache.scale.impl.RedisNode;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.ParseServersUtil;

public class RedisDashBoardUtil {
	
	private static  CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
	
	private static List<RedisClusterData> data = new ArrayList<RedisClusterData>();
	
	public static  List<RedisClusterData> getClusterData(){
		List<RedisClusterData> data = new ArrayList<RedisClusterData>();
		//get all redis-cluster
		List<CacheConfiguration> configList = cacheConfigurationService.findAll();
		//get all redis-master-server
		for (CacheConfiguration item : configList) { 
			if(item.getCacheKey().startsWith("redis")){
				RedisClusterData tmp = new RedisClusterData();
				
				
				String url = item.getServers();
				List<String> servers = ParseServersUtil.parseRedisServers(url);
				RedisCluster redisCluster = new RedisCluster(servers);
				redisCluster.loadClusterInfo();
				List<RedisNode> nodes = redisCluster.getNodes();
				
				long maxmemory = 0,usedmemory=0;
				for(RedisNode node : nodes){
					node.getMaster().loadRedisInfo();
					maxmemory += node.getMaster().getInfo().getMaxMemory();
					usedmemory += node.getMaster().getInfo().getUsedMemory();
				}
				
				tmp.setClusterName(item.getCacheKey());
				tmp.setNodes(nodes);
				tmp.setMaxMemory(maxmemory / 1024);
				tmp.setUsedMemory(usedmemory/1024);
				float used = (float)usedmemory/maxmemory;
				used = convert(used);
				tmp.setUsed(used);
				tmp.check();
				data.add(tmp);
			}
		}
		return data;
	}
	
	public static Map<String,Object> getRedisServerData(String address){
		Jedis jedis = RedisConnectionFactory.getConnection(address);
		String info = jedis.info();
		return parseRedisInfo(info);
	}
	
	
	private static Map<String,Object> parseRedisInfo(String infoString){
		Map<String,Object> data = new HashMap<String,Object>();
		String[] infoArray = infoString.split("\r\n");
		for(String info : infoArray){
			info.trim();
			String[] each = info.split(":");
			if(each.length > 1)
				data.put(each[0], each[1]);
		}
		return data;
	}

	private static float convert(float value){
		int tmp = Math.round(value*10000);
		return (float)(tmp/100.0);
	}
	
	public static void main(String[] args){
		Jedis jedis = RedisConnectionFactory.getConnection("10.3.21.26:");
		String info = jedis.info();
		List<String> data = jedis.configGet("*");
		for(String a : data){
			System.out.println(a);
		}
		System.out.println(info);
		parseRedisInfo(info);
	}
	
}
