package com.dianping.cache.dao;

import com.dianping.cache.entity.MemcacheStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemcacheStatsDao {

	List<MemcacheStats> findAll();
	
	List<MemcacheStats> findByServer(String server);
	
	List<MemcacheStats> findByServerWithInterval(@Param("address")String address,@Param("start")long start,@Param("end")long end);
	
	void insert(MemcacheStats data);
	
	MemcacheStats findLast(String server);

	void delete(long timeBefore);

	/**
	 * @param sql
	 * @return
	 */
	List<MemcacheStats> search(@Param("paramSQL") String sql);
}
