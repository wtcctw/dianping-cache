package com.dianping.cache.alarm.dataanalyse;

/**
 * Created by lvshiyun on 15/12/31.
 */
public interface BaselineService {

    Baseline getBaseline(String name);

    void putBaseline(String name, Baseline baseline);

}
