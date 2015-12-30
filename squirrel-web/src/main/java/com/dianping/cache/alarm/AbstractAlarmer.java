package com.dianping.cache.alarm;

import com.dianping.cache.alarm.lifecycle.AbstractLifeCycle;
import com.dianping.cache.alarm.lifecycle.AlarmerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by lvshiyun on 15/11/21.
 */
public abstract class AbstractAlarmer extends AbstractLifeCycle implements AlarmerLifecycle {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String DOALARM_FUNCTION = "-doAlarm";

    @Autowired
    protected AlarmerTaskManager alarmerTaskManager;

    protected String alarmName;

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        alarmName = getClass()+DOALARM_FUNCTION;

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        logger.info("[doStart] {} start."+ getClass().getSimpleName());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        logger.info("[doStop] {} stop."+getClass().getSimpleName());
    }
}
