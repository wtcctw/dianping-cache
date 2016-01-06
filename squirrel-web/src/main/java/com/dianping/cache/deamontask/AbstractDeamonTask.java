package com.dianping.cache.deamontask;

import com.dianping.cache.entity.DeamonTask;

import java.util.concurrent.Callable;

/**
 * Created by thunder on 16/1/5.
 */
public abstract class AbstractDeamonTask<T> implements Runnable{

    private DeamonTask deamonTask;

    protected void startTask() {

    }

    protected void endTask() {

    }

    public abstract void taskRun();

    public void run() {
        startTask();
        taskRun();
        endTask();
    }



}
