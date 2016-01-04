package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.MemcacheBaseline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemcacheBaselineDao {

	List<MemcacheBaseline> findAll();
	
	List<MemcacheBaseline> findByServer(String server);
	
	List<MemcacheBaseline> findByServerWithInterval(@Param("address") String address, @Param("start") long start, @Param("end") long end);
	
	void insert(MemcacheBaseline data);
	
	MemcacheBaseline findLast(String server);

	void delete(long timeBefore);

	/**
	 * @param sql
	 * @return
	 */
	List<MemcacheBaseline> search(@Param("paramSQL") String sql);
}
