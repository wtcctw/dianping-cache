package com.dianping.squirrel.task;

import com.dianping.cache.service.RdbService;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by thunder on 16/1/6.
 */
public class ClearCategoryTask extends AbstractTask {

    private String TASKTYPE = "CLEAR_CATEGORY";
    private String category;
    private StoreClient storeClient;

    @Autowired
    private RdbService rdbService;

    public ClearCategoryTask() {
    }

    public ClearCategoryTask(String category) {
        this.category = category;
        this.storeClient = StoreClientFactory.getStoreClientByCategory(category);
    }

    @Override
    String getTaskType() {
        return TASKTYPE + " : " + this.category;
    }

    @Override
    int getTaskMinStat() {
        return 0;
    }

    @Override
    long getTaskMaxStat() {
        try {
            RdbService.TotalStat totalStat = rdbService.getCategoryMergeStat(category);
            return totalStat.count;
        } catch (Exception e) {
            return 2000;
        }
    }

    @Override
    public void taskRun() {

        if (storeClient == null || !(storeClient instanceof RedisStoreClient)) {
            return;
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        JedisCluster jedisCluster = ((RedisStoreClient) storeClient).getJedisClient();
        Set<HostAndPort> nodes = jedisCluster.getCluserNodesHostAndPort();
        final AtomicLong stat = new AtomicLong(0);
        final int step = 2000;
        Set<HostAndPort> masters = new HashSet<HostAndPort>();

        for (HostAndPort node : nodes) {
            Jedis jedis = new Jedis(node.getHost(), node.getPort());
            try {
                String info = jedis.info("Replication");
                if (info.indexOf("role:master") < 0)
                    continue;
                masters.add(node);
            } catch (Throwable t) {
                continue;
            }
        }

        final CountDownLatch latch = new CountDownLatch(masters.size());
        ArrayList<Future> masterTask = new ArrayList<Future>();

        for (final HostAndPort node : masters) {
            Future f = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Jedis jedis = new Jedis(node.getHost(), node.getPort());
                    ScanParams scanParams = new ScanParams();
                    scanParams.match(category + "*");
                    scanParams.count(step);
                    ScanResult<String> result;
                    result = jedis.scan("0", scanParams);
                    boolean isInterrupted = Thread.currentThread().isInterrupted();
                    boolean isEnd = result.getStringCursor().equals("0");
                    while (!isInterrupted && !isEnd) {
                        try {
                            for (String key : result.getResult()) {
                                jedis.del(key);
                                stat.addAndGet(1);
                                if (stat.get() % 10000 == 0) {
                                    synchronized (stat) {
                                        if(stat.get() % 10000 == 0)
                                            ClearCategoryTask.this.updateStat((int) stat.get());
                                    }
                                }
                            }
                            result = jedis.scan(result.getStringCursor(), scanParams);
                        } catch (Throwable t) {
                            continue;
                        }
                    }
                    latch.countDown();
                }
            });
            masterTask.add(f);
        }
        while(latch.getCount() != 0) {
            try {
                latch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                for(Future f : masterTask) {
                    f.cancel(true);
                }
                executorService.shutdownNow();
            }
        }
        executorService.shutdownNow();
    }

    public static void main(String[] args) {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        for (int i = 0; i < 2000; i++) {
            storeClient.set(new StoreKey("redis-del", i), i);
        }
        // 192.168.217.36 7004

//        Jedis jedis = new Jedis("192.168.211.63", 7001);
//        String s = jedis.info("Replication");
//        boolean b = jedis.isConnected();
//        int a = 1;
    }

}