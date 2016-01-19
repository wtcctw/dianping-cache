package com.dianping.cache.alarm.event;

import com.dianping.cache.alarm.event.Event;

/**
 * Created by lvshiyun on 15/11/27.
 */
public interface EventReporter {
    public void report(Event event);
}
