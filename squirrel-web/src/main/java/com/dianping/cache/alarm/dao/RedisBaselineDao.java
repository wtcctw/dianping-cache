package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.RedisBaseline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RedisBaselineDao {
	
	List<RedisBaseline> findAll();

	List<RedisBaseline>findByTaskId(int taskId);
	
	List<RedisBaseline> findByName(String baseline_name);
	
	List<RedisBaseline> findByServerWithInterval(@Param("address") String address, @Param("start") long start, @Param("end") long end);
	
	void insert(RedisBaseline data);
	
	RedisBaseline findLast(String server);

	void delete(long timeBefore);

	/**
	 * @param sql
	 * @return
	 */
	List<RedisBaseline> search(@Param("paramSQL") String sql);
}
