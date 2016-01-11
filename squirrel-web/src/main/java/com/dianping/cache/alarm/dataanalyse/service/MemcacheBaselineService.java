package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.entity.MemcacheBaseline;

import java.util.List;

public interface MemcacheBaselineService {

	List<MemcacheBaseline> findAll();

	List<MemcacheBaseline> findByTaskId(int taskId);
	
	List<MemcacheBaseline> findByName(String baseline_name);
	
	void insert(MemcacheBaseline stat);
	
	List<MemcacheBaseline> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);

	List<MemcacheBaseline>search(String sql);
}
