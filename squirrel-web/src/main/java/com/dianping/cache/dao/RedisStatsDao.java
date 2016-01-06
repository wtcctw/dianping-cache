package com.dianping.cache.dao;

import com.dianping.cache.entity.RedisStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RedisStatsDao {
	
	List<RedisStats> findAll();
	
	List<RedisStats> findByServer(String server);
	
	List<RedisStats> findByServerWithInterval(@Param("address")String address,@Param("start")long start,@Param("end")long end);
	
	void insert(RedisStats data);
	
	RedisStats findLast(String server);

	void delete(long timeBefore);

	/**
	 * @param sql
	 * @return
	 */
	List<RedisStats> search(@Param("paramSQL") String sql);
}
