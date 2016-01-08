package com.dianping.cache.alarm.threadmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lvshiyun on 16/1/7.
 */
public class ThreadManager {

    private static ExecutorService instance = Executors.newFixedThreadPool(10);

    public static synchronized ExecutorService getInstance(){
        if(null == instance){
            return Executors.newFixedThreadPool(10);
        }
        return instance;
    }

}
