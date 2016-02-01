package com.dianping.cache.service;

import com.dianping.cache.entity.RedisStats;

import java.util.List;

public interface RedisService {
	List<RedisStats> findByServer(String server);
	
	void insert(RedisStats stat);
	
	List<RedisStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);

	List<RedisStats>search(String sql);

	List<RedisStats> findPeriodicStats(String address, long end, int period);
}
