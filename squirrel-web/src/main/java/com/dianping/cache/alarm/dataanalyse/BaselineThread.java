package com.dianping.cache.alarm.dataanalyse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/7.
 */
@Component("baselineThread")
@Scope("prototype")
public class BaselineThread implements Runnable{

    @Autowired
    BaselineComputeTaskFactory baselineComputeTaskFactory;

    @Override
    public void run() {
        BaselineComputeTask baselineComputeTask = baselineComputeTaskFactory.createBaselineComputeTask();
        baselineComputeTask.run();
    }
}
