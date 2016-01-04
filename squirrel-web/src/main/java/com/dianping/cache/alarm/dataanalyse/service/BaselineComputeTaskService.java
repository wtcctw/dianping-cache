package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.entity.BaselinecomputeTask;

import java.util.List;

public interface BaselineComputeTaskService {
	

	void insert(BaselinecomputeTask baselinecomputeTask);

	List<BaselinecomputeTask> getRecentTaskId();

}
