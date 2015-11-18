package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.MemcacheStats;

public interface MemcacheStatsService {
	
	public List<MemcacheStats> findByServer(String server);
	
	public void insert(MemcacheStats stat);
	
	public List<MemcacheStats> findByServerWithInterval(String address,long start,long end);
	
	public void delete(long timeBefore);
}
