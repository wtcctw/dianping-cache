package com.dianping.cache.alarm.dataanalyse.task;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/3.
 */
@Component
public class BaselineComputeTaskFactoryImpl implements BaselineComputeTaskFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public BaselineComputeTask createBaselineComputeTask(){
        BaselineComputeTask baselineComputeTask=(BaselineComputeTask)applicationContext.getBean("baselineComputeTask");
        return baselineComputeTask;
    }
}
