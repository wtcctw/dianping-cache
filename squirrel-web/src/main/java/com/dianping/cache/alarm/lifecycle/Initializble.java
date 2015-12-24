package com.dianping.cache.alarm.lifecycle;

/**
 * Created by lvshiyun on 15/11/30.
 */
public interface Initializble {

    public static String PHASE_NAME = "initialized";

    void initialize() throws Exception;
}
