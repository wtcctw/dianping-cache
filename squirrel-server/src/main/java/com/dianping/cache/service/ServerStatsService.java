package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.ServerStats;

public interface ServerStatsService {
	public List<ServerStats> findByServer(String server);
	
	public void insert(ServerStats stat);
	
	public List<ServerStats> findByServerWithInterval(String address,long start,long end);
	
	public void delete(long timeBefore);
}
