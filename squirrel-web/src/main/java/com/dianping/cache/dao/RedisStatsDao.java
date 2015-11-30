package com.dianping.cache.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dianping.cache.entity.RedisStats;

public interface RedisStatsDao {
	
	List<RedisStats> findAll();
	
	List<RedisStats> findByServer(String server);
	
	List<RedisStats> findByServerWithInterval(@Param("address")String address,@Param("start")long start,@Param("end")long end);
	
	void insert(RedisStats data);
	
	RedisStats findLast(String server);

	void delete(long timeBefore);
}
