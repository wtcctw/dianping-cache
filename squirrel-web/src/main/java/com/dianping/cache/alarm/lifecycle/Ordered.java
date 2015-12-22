package com.dianping.cache.alarm.lifecycle;

/**
 * Created by lvshiyun on 15/11/30.
 */
public interface Ordered {

    public static final int FIRST = Integer.MIN_VALUE;

    public static final int LAST = Integer.MAX_VALUE;

    int getOrder();
}
