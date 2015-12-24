package com.dianping.cache.alarm.event;

/**
 * Created by lvshiyun on 15/11/27.
 */
public enum EventType {
    MEMCACHE,

    REDIS;

    public boolean isMemcacheType() {
        return this == MEMCACHE;
    }

    public boolean isRedisType() {
        return this == REDIS;
    }
}
