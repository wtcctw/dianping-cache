package com.dianping.cache.alarm;

import com.dianping.cache.alarm.lifecycle.AbstractLifeCycle;
import com.dianping.cache.alarm.lifecycle.AlarmerLifecycle;
import com.dianping.cache.alarm.utils.CommonUtils;
import com.dianping.cache.alarm.utils.ThreadFactoryUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Created by lvshiyun on 15/11/23.
 */
@Component
public class AlarmerTaskManagerImpl extends AbstractLifeCycle implements AlarmerTaskManager, AlarmerLifecycle {

    protected final static String EXECUTOR_FACTORY_NAME = "ExecutorAlamer";

    protected final static String SCHEDULED_FACTORY_NAME = "ScheduleAlamer";

    private ExecutorService executor = null;

    private ScheduledExecutorService scheduled = null;

    @Override
    protected void doInitialize() throws Exception {
        executor = Executors.newFixedThreadPool(CommonUtils.DEFAULT_CPU_COUNT*2,
                ThreadFactoryUtils.getThreadFactory(EXECUTOR_FACTORY_NAME));
        scheduled = Executors.newScheduledThreadPool(CommonUtils.DEFAULT_CPU_COUNT*2,
                ThreadFactoryUtils.getThreadFactory(SCHEDULED_FACTORY_NAME));

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        logger.info("[doStart] {} start.", getClass().getSimpleName());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        logger.info("[doStop] {} stop.", getClass().getSimpleName());
    }
    @Override
    protected void doDispose() throws Exception{
        super.doDispose();
        if(null != executor && !executor.isShutdown()){
            executor.shutdown();
        }
        if(null != scheduled && !scheduled.isShutdown()){
            scheduled.shutdown();
        }
    }

    @Override
    public Future<?> submit(Runnable command) {
        return executor.submit(command);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduled.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduled.scheduleWithFixedDelay(command,initialDelay, delay, unit);
    }
}
