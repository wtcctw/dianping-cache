package com.dianping.cache.alarm.dataanalyse.thread;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 16/1/7.
 */
@Component
public class BaselineThreadFactoryImpl implements BaselineThreadFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;

    }

    @Override
    public BaselineThread createBaselineThread() {

        BaselineThread baselineThread = (BaselineThread)applicationContext.getBean("baselineThread");
        return baselineThread;

    }
}
