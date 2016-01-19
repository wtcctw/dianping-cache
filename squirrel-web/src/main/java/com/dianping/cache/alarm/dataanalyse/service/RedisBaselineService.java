package com.dianping.cache.alarm.dataanalyse.service;


import com.dianping.cache.alarm.entity.RedisBaseline;

import java.util.List;

public interface RedisBaselineService {

	List<RedisBaseline> findAll();

	List<RedisBaseline> findByTaskId(int taskId);

	List<RedisBaseline> findByName(String baseline_name);
	
	void insert(RedisBaseline stat);
	
	List<RedisBaseline> findByServerWithInterval(String address, long start, long end);
	
	void delete(long timeBefore);

	List<RedisBaseline>search(String sql);
	
}
