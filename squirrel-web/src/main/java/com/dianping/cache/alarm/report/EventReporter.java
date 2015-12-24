package com.dianping.cache.alarm.report;

import com.dianping.cache.alarm.event.Event;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 15/11/27.
 */
public interface EventReporter {
    public void report(Event event);
}
