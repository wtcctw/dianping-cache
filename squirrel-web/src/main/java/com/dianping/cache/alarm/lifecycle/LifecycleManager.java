package com.dianping.cache.alarm.lifecycle;


import com.dianping.swallow.common.internal.lifecycle.LifecycleCallback;

/**
 * 获取当前组件的当前生命周期
 * Created by lvshiyun on 15/11/30.
 */
public interface LifecycleManager {

    public static String CREATED_PHASE_NAME = "created";

    void initialize(LifecycleCallback callback) throws Exception;

    void start(LifecycleCallback callback) throws Exception;

    void stop(LifecycleCallback callback) throws Exception;

    void dispose(LifecycleCallback callback) throws Exception;

    boolean isCreated();

    boolean isInitialized();

    boolean isStarted();

    boolean isStopped();

    boolean isDisposed();

    String getCurrentPhaseName();
}
