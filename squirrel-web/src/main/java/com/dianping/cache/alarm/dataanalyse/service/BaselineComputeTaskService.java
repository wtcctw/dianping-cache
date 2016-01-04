package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.entity.BaselineComputeTask;

import java.util.List;

public interface BaselineComputeTaskService {
	

	void insert(BaselineComputeTask baselinecomputeTask);

	List<BaselineComputeTask> getRecentTaskId();

}
