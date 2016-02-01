package com.dianping.cache.alarm.dataanalyse.task;

/**
 * Created by lvshiyun on 16/1/3.
 */
public interface BaselineTaskFactory {
    BaselineComputeTask createBaselineComputeTask();

    BaselineCleanTask createBaselineCleanTask();
}
