package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.MemcacheStats;

public interface MemcacheStatsService {
	
	List<MemcacheStats> findByServer(String server);
	
	void insert(MemcacheStats stat);
	
	List<MemcacheStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);
}
