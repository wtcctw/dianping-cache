package com.dianping.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cache.entity.ServerStats;

public interface ServerStatsDao {
	List<ServerStats> findAll();
	
	List<ServerStats> findByServer(String server);
	
	List<ServerStats> findByServerWithInterval(@Param("address")String address,@Param("start")long start,@Param("end")long end);
	
	public void insert(ServerStats data);
	
	public void delete(long timeBefore);
	
	public ServerStats findLast(String server);
}
