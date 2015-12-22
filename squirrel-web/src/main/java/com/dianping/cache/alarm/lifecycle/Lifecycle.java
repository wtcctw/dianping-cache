package com.dianping.cache.alarm.lifecycle;


import com.dianping.swallow.common.internal.lifecycle.Disposable;
import com.dianping.swallow.common.internal.lifecycle.Startable;
import com.dianping.swallow.common.internal.lifecycle.Stopable;

/**
 * 组件生命周期控制
 * Created by lvshiyun on 15/11/30.
 */
public interface Lifecycle extends Initializble, Startable, Stopable, Disposable, Ordered {
}
