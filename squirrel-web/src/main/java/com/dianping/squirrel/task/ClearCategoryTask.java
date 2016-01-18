package com.dianping.squirrel.task;

import com.dianping.cache.util.RequestUtil;
import com.dianping.cache.util.SpringLocator;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;
import com.dianping.squirrel.dao.TaskDao;
import com.dianping.squirrel.entity.Task;

import redis.clients.jedis.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by thunder on 16/1/6.
 */
public class ClearCategoryTask extends AbstractTask {

    private String category;
    private StoreClient storeClient;

    private TaskDao taskDao = SpringLocator.getBean("taskDao");

    public ClearCategoryTask(){
    }

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
        task.setStatMax(2000);
        task.setCommiter(RequestUtil.getUsername());
        taskDao.insert(task);

        this.task = task;
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
    public static void main(String[] args) {
        ClearCategoryTask task = new ClearCategoryTask("redis-del");
        task.run();
    }
}