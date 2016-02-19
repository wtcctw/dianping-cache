package com.dianping.cache.alarm.dataanalyse.thread;

/**
 * Created by lvshiyun on 16/1/7.
 */
public interface BaselineThreadFactory {

    BaselineComputeThread createBaselineComputeThread();

    BaselineCleanThread createBaselineCleanThread();

    BaselineMapGetThread createBaselineMapGetThread();
}
