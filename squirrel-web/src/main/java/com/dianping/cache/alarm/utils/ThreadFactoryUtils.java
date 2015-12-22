package com.dianping.cache.alarm.utils;



import java.util.concurrent.ThreadFactory;

/**
 * Created by lvshiyun on 15/11/23.
 */
public class ThreadFactoryUtils {

    public static ThreadFactory getThreadFactory(final String name){
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return ThreadUtils.createThread(runnable,name,true);
            }
        };
    }
}
