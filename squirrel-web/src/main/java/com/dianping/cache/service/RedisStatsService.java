package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.RedisStats;

public interface RedisStatsService {
	public List<RedisStats> findByServer(String server);
	
	public void insert(RedisStats stat);
	
	public List<RedisStats> findByServerWithInterval(String address,long start,long end);
	
	public void delete(long timeBefore);
	
}
