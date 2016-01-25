package com.dianping.cache.service;

import com.dianping.cache.entity.MemcachedStats;

import java.util.List;

public interface MemcachedStatsService {
	
	List<MemcachedStats> findByServer(String server);
	
	void insert(MemcachedStats stat);
	
	List<MemcachedStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);

	List<MemcachedStats>search(String sql);
}
