package com.dianping.cache.alarm.dataanalyse.thread;

import com.dianping.cache.alarm.dataanalyse.task.BaselineMapGetTask;
import com.dianping.cache.alarm.dataanalyse.task.BaselineTaskFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/7.
 */
@Component("baselineMapGetThread")
@Scope("prototype")
public class BaselineMapGetThread implements Runnable{

    @Autowired
    BaselineTaskFactory baselineTaskFactory;

    @Override
    public void run() {
        BaselineMapGetTask baselineMapGetTask = baselineTaskFactory.createBaselineMapGetTask();
        baselineMapGetTask.run();
    }
}
