package com.dianping.cache.alarm.report.thread;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/13.
 */
@Component
public class ScanThreadFactoryImpl implements ScanThreadFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public ScanThread createScanThread() {
        ScanThread scanThread = (ScanThread)applicationContext.getBean("scanThread");

        return scanThread;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
