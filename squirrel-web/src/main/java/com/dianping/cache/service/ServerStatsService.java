package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.ServerStats;

public interface ServerStatsService {
	List<ServerStats> findByServer(String server);
	
	void insert(ServerStats stat);
	
	List<ServerStats> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);
}
