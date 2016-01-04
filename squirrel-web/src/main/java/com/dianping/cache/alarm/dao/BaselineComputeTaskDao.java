package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.BaselinecomputeTask;

import java.util.List;

public interface BaselineComputeTaskDao {

	/**
	 * @param baselinecomputeTask
	 * @return
	 */
	void insert(BaselinecomputeTask baselinecomputeTask);

	/**
	 * @param
	 * @return
	 */
	List<BaselinecomputeTask> getRecentTaskId();

}
