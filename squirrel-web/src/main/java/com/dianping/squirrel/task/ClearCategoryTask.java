package com.dianping.squirrel.task;

import com.dianping.cache.service.RdbService;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;

import net.sf.ehcache.search.aggregator.Count;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by thunder on 16/1/6.
 */
public class ClearCategoryTask extends AbstractTask {
    private static Logger logger = LoggerFactory.getLogger(ClearCategoryTask.class);

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
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        ExecutorService executorService = Executors.newCachedThreadPool();
        JedisCluster jedisCluster = ((RedisStoreClient) storeClient).getJedisClient();
        Set<HostAndPort> nodes = jedisCluster.getCluserNodesHostAndPort();
        final AtomicLong stat = new AtomicLong(1);
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
                    while (!isInterrupted && !result.getStringCursor().equals("0")) {
                        try {
                            List<Response<Long>> responses = new ArrayList<Response<Long>>();
                            Pipeline pipeline = jedis.pipelined();
                            for (String key : result.getResult()) {
                                Response<Long> rl = pipeline.del(key);
                                responses.add(rl);
                            }
                            pipeline.sync();
                            for(Response<Long> r : responses) {
                                stat.addAndGet(r.get());
                            }
                            logger.info("category " + category + " Stat " + stat + " cursor " + result.getStringCursor());
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

    public static void addData() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        int total = 1000;
        final int step = 1000;
        final CountDownLatch latch = new CountDownLatch(total);
        for (int i = 0; i < total; i ++) {
            List<StoreKey> list = new ArrayList<StoreKey>();
            List<Integer> res = new ArrayList<Integer>();
            for(int j = 0; j < step; j ++) {
                list.add(new StoreKey("redis-del", step * i + j));
                res.add(j);
            }
            storeClient.asyncMultiSet(list, res, new StoreCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    latch.countDown();
                    System.out.println("getback " + latch.getCount());
                }

                @Override
                public void onFailure(Throwable e) {
                    latch.countDown();
                    System.out.println("getback " + latch.getCount());
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        taskRun1("redis-del");
//        addData();
    }

}