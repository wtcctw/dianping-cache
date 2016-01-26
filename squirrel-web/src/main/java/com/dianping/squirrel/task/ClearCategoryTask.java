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
        JedisCluster jedisCluster = ((RedisStoreClient) storeClient).getJedisClient();
        Set<HostAndPort> nodes = jedisCluster.getCluserNodesHostAndPort();
        long stat = 0;
        int step = 2000;
        for (HostAndPort node : nodes) {
            System.out.println(node.getHost() + " " + node.getPort());
            Jedis jedis = new Jedis(node.getHost(), node.getPort());
            try {
                String info = jedis.info("Replication");
                if(info.indexOf("role:master") < 0)
                    continue;
            } catch (Throwable t) {
                continue;
            }
            ScanParams scanParams = new ScanParams();
            scanParams.match(category + "*");
            scanParams.count(step);
            ScanResult<String> result;
            try {
                result = jedis.scan("0", scanParams);
            } catch (Throwable e) {
                continue;
            }
            boolean a = Thread.currentThread().isInterrupted();
            boolean b = result.getStringCursor().equals("0");
            while (!Thread.currentThread().isInterrupted() && !b) {
                try {
                    for (String key : result.getResult()) {
                        jedis.del(key);
                        stat += 1;
                    }
                    if(stat % 1000 == 0)
                        this.updateStat((int)stat);
                    result = jedis.scan(result.getStringCursor(), scanParams);
                } catch (Throwable t) {
                    continue;
                }
            }
            this.updateStat((int)stat);
        }
    }

    public static void main(String[] args) {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        for(int i = 0; i < 2000; i++) {
            storeClient.set(new StoreKey("redis-del", i), i);
        }
        // 192.168.217.36 7004

//        Jedis jedis = new Jedis("192.168.211.63", 7001);
//        String s = jedis.info("Replication");
//        boolean b = jedis.isConnected();
//        int a = 1;
    }

}