package com.dianping.cache.alarm.dataanalyse.thread;

import com.dianping.cache.alarm.dataanalyse.task.BaselineCleanTask;
import com.dianping.cache.alarm.dataanalyse.task.BaselineTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/7.
 */
@Component("baselineCleanThread")
@Scope("prototype")
public class BaselineCleanThread implements Runnable{

    @Autowired
    BaselineTaskFactory baselineTaskFactory;

    @Override
    public void run() {
        BaselineCleanTask baselineCleanTask = baselineTaskFactory.createBaselineCleanTask();
        baselineCleanTask.run();
    }
}
