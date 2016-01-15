package com.dianping.cache.alarm.report.task;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/12.
 */
@Component
public class ScanTaskFactoryImpl implements ScanTaskFactory,ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public ScanTask createScanTask() {
        ScanTask scanTask = (ScanTask)applicationContext.getBean("scanTask");

        return scanTask;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
