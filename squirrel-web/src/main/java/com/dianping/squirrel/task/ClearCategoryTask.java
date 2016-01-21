package com.dianping.squirrel.task;

import com.dianping.cache.service.RdbService;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.*;

import java.util.Set;

/**
 * Created by thunder on 16/1/6.
 */
public class ClearCategoryTask extends AbstractTask {

    private String TASKTYPE = "CLEAR_CATEGORY";
    private String category;
    private StoreClient storeClient;

    @Autowired
    private RdbService rdbService;

    public ClearCategoryTask(){
    }

    public ClearCategoryTask(String category) {
        this.category = category;
        this.storeClient = StoreClientFactory.getStoreClientByCategory(category);
    }

    @Override
    String getTaskType() {
        return TASKTYPE;
    }

    @Override
    int getTaskMinStat() {
        return 0;
    }

    @Override
    long getTaskMaxStat() {
        RdbService.TotalStat totalStat = rdbService.getCategoryMergeStat(category);
        return totalStat.count;
    }

    @Override
    public void taskRun() {
        if (storeClient == null || !(storeClient instanceof RedisStoreClient)) {
            return;
        }
        JedisCluster jedisCluster = ((RedisStoreClient) storeClient).getJedisClient();
        Set<HostAndPort> nodes = jedisCluster.getCluserNodesHostAndPort();
        long stat = 0;
        int step = 50;
        for (HostAndPort node : nodes) {
            System.out.println(node.getHost());
            Jedis jedis = new Jedis(node.getHost(), node.getPort());
            ScanParams scanParams = new ScanParams();
            scanParams.match(category + "*");
            scanParams.count(step);
            ScanResult<String> result;
            try {
                result = jedis.scan("0", scanParams);
            } catch (Throwable e) {
                continue;
            }
            while (!Thread.currentThread().isInterrupted() && !result.getStringCursor().equals("0")) {
                for(String key : result.getResult()) {
                    jedis.del(key);
                }
                result = jedis.scan(result.getStringCursor(), scanParams);
            }
            stat += step;
            this.updateStat((int)stat);
        }
    }

    public static void main(String[] args) {
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory("redistemp");
        for(int i = 0; i < 1000; i++) {
            StoreKey storeKey = new StoreKey(Integer.toString(i));
            storeClient.add(storeKey, "123");
        }
    }

}