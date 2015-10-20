package com.dianping.cache.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {

    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    
    public static void run(Runnable task) {
        threadPool.submit(task);
    }
    
}
