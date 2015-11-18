package com.dianping.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cache.entity.MemcacheStats;

public interface MemcacheStatsDao {

	List<MemcacheStats> findAll();
	
	List<MemcacheStats> findByServer(String server);
	
	List<MemcacheStats> findByServerWithInterval(@Param("address")String address,@Param("start")long start,@Param("end")long end);
	
	public void insert(MemcacheStats data);
	
	public MemcacheStats findLast(String server);

	void delete(long timeBefore);
}
