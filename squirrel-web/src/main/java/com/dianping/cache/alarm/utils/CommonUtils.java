package com.dianping.cache.alarm.utils;

/**
 * Created by lvshiyun on 15/11/23.
 */
public class CommonUtils {

    public static final int DEFAULT_CPU_COUNT = 4;

    public static int getCpuCount(){
        int cpuCount = Runtime.getRuntime().availableProcessors();
        if(cpuCount<=0){
            cpuCount = 4;
        }
        return cpuCount;
    }
}
