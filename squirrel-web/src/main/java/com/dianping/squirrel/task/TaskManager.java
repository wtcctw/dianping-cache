package com.dianping.squirrel.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thunder on 16/1/5.
 */
public class TaskManager {
    private static ThreadPoolExecutor executor;

    private static int DEFAULT_WATING_QUEUE_SIZE = 16384;
    private static int MAX_POOL_SIZE = 20;
    private static int CORE_POOL_SIZE = 4;
    private static int KEEP_ALIVE_TIME = 20;

    static {
        executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME
                , TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(DEFAULT_WATING_QUEUE_SIZE), new ThreadFactory() {
            final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "cache-task-" + Integer.toString(this.threadNumber.getAndIncrement()));
                thread.setDaemon(true);
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void submit(AbstractTask task) {
        executor.submit(task);
    }

}
