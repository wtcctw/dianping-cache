package com.dianping.cache.service;

import com.dianping.cache.entity.MemcacheStats;

import java.util.List;

public interface MemcacheStatsService {
	
	List<MemcacheStats> findByServer(String server);
	
	void insert(MemcacheStats stat);
	
	List<MemcacheStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);

	List<MemcacheStats>search(String sql);
}
