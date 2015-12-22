package com.dianping.cache.alarm.event;

/**
 * Created by lvshiyun on 15/11/23.
 */
public interface EventChannel {

    public void put(Event event) throws InterruptedException;

    public Event next() throws InterruptedException;
}
