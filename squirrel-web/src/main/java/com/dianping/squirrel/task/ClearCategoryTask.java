package com.dianping.squirrel.task;

import com.dianping.cache.dao.TaskDao;
import com.dianping.cache.entity.Task;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;

import redis.clients.jedis.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by thunder on 16/1/6.
 */
public class ClearCategoryTask extends AbstractTask {

    private String category;
    private StoreClient storeClient;
    @Resource(name = "taskDao")
    private TaskDao taskDao;

    public ClearCategoryTask(String category) {
        this.category = category;
        this.storeClient = StoreClientFactory.getStoreClientByCategory(category);
    }

    @Override
    public void startTask() {
        Task task = new Task();
        task.setCommitTime(System.currentTimeMillis());
        task.setCommiter("nobody");
        task.setType(TaskType.CLEAR_CATEGORY.ordinal());
        task.setStatMax(20000);
        taskDao.insert(task);
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
            Jedis jedis = new Jedis(node.getHost(), node.getPort());
            ScanParams scanParams = new ScanParams();
            scanParams.match(category + "*");
            scanParams.count(step);
            ScanResult<String> result;
            result = jedis.scan("0", scanParams);
            while (!Thread.currentThread().isInterrupted() && !result.getStringCursor().equals("0")) {
                jedis.del(result.getResult().toArray(new String[0]));
                result = jedis.scan(result.getStringCursor(), scanParams);
            }
            stat += step;
            Map<String, String> para = new HashMap<String, String>();
            para.put("id", Integer.toString(this.task.getId()));
            para.put("stat", Long.toString(stat));
            taskDao.updateStat(para);
        }
    }

    @Override
    public void endTask(){
        Map<String, String> para = new HashMap<String, String>();
        para.put("endTime", Long.toString(System.currentTimeMillis()));
        para.put("id", Integer.toString(this.task.getId()));
        taskDao.updateEndTime(para);
    }
}