package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.BaselineComputeTask;

import java.util.List;

public interface BaselineComputeTaskDao {

	/**
	 * @param baselinecomputeTask
	 * @return
	 */
	void insert(BaselineComputeTask baselinecomputeTask);

	/**
	 * @param
	 * @return
	 */
	List<BaselineComputeTask> getRecentTaskId();

}
