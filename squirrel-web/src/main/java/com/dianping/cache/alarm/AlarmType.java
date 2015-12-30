package com.dianping.cache.alarm;

/**
 * Created by lvshiyun on 15/11/22.
 */
public enum AlarmType {
    MEMCACHE_CLUSTER_DOWN(0),

    MEMCACHE_MEMUSAGE_TOO_HIGH(1),

    MEMCACHE_QPS_TOO_HIGH(2),

    MEMCACHE_CONN_TOO_HIGH(3),

    REDIS_CLUSTER_DOWN(4),

    REDIS_MEMUSAGE_TOO_HIGH(5),

    REDIS_QPS_TOO_HIGH(6);

    private int number;

    private AlarmType(){

    }

    private AlarmType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }
}
