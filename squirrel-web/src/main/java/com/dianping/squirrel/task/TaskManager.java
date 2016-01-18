package com.dianping.squirrel.task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
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
    private static Map<Integer, Future> futureMap = new HashMap<Integer, Future>();

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
        Future f = executor.submit(task);
        futureMap.put(task.getTaskId(), f);
    }

    public static void cancelTask(int id) {
        Future f = futureMap.get(id);
        f.cancel(true);
        futureMap.remove(id);
    }

}
