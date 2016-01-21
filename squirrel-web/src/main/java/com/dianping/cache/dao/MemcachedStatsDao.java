package com.dianping.cache.dao;

import com.dianping.cache.entity.MemcachedStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemcachedStatsDao {

	List<MemcachedStats> findAll();
	
	List<MemcachedStats> findByServer(String server);
	
	List<MemcachedStats> findByServerWithInterval(@Param("address")String address, @Param("start")long start, @Param("end")long end);
	
	void insert(MemcachedStats data);
	
	MemcachedStats findLast(String server);

	void delete(long timeBefore);

	/**
	 * @param sql
	 * @return
	 */
	List<MemcachedStats> search(@Param("paramSQL") String sql);
}
