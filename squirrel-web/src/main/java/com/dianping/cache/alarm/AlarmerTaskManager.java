package com.dianping.cache.alarm;

import org.apache.http.impl.client.FutureRequestExecutionMetrics;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by lvshiyun on 15/11/21.
 */

public interface AlarmerTaskManager {
    Future<?> submit(Runnable command);
    ScheduledFuture<?>scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit);
    ScheduledFuture<?>scheduleWithFixedDelay(Runnable command,long initialDelay,long delay,TimeUnit unit);
}
