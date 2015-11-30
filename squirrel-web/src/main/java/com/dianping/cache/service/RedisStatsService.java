package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.RedisStats;

public interface RedisStatsService {
	List<RedisStats> findByServer(String server);
	
	void insert(RedisStats stat);
	
	List<RedisStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);
	
}
