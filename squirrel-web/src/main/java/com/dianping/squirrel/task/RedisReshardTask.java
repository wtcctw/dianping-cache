package com.dianping.squirrel.task;

import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.scale.cluster.redis.RedisCluster;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import com.dianping.cache.scale.cluster.redis.ReshardPlan;
import com.dianping.cache.service.ReshardService;
import com.dianping.cache.util.RequestUtil;
import com.dianping.cache.util.SpringLocator;
import com.dianping.squirrel.dao.TaskDao;
import com.dianping.squirrel.entity.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dp on 16/1/6.
 */
public class RedisReshardTask extends AbstractTask {

    private Logger logger = LoggerFactory.getLogger(RedisReshardTask.class);

    private ReshardPlan reshardPlan;

    private ReshardService reshardService;

    private TaskDao taskDao;

    public RedisReshardTask(ReshardPlan reshardPlan) {
        this.reshardPlan = reshardPlan;
        taskDao = SpringLocator.getBean("taskDao");
        reshardService = SpringLocator.getBean("reshardService");
    }

    @Override
    protected void startTask() {
        Task task = new Task();
        task.setCommitTime(System.currentTimeMillis());
        task.setCommiter("nobody");
        task.setType(TaskType.RESHARD.ordinal());
        task.setStatMax(2000);
        task.setDescription("ReshardTask");
        task.setCommiter(RequestUtil.getUsername());
        taskDao.insert(task);
        this.task = task;
    }

    @Override
    protected void endTask() {
        Map<String, String> para = new HashMap<String, String>();
        para.put("endTime", Long.toString(System.currentTimeMillis()));
        para.put("id", Integer.toString(this.task.getId()));
        taskDao.updateEndTime(para);
    }

    @Override
    public void taskRun() {
        reshard();
    }

    private void reshard() {
        RedisCluster redisCluster = new RedisCluster(reshardPlan.getSrcNode());
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            while (!Thread.currentThread().isInterrupted() ) {
                try {
                    redisCluster.loadClusterInfo();
                    RedisServer src = redisCluster.getServer(reshardRecord.getSrcNode());
                    if (src == null)
                        continue;
                    if (src.getSlotSize() == 0)
                        break;
                    RedisServer des = redisCluster.getServer(reshardRecord.getDesNode());
                    if (reshardRecord.getSlotsDone() < reshardRecord.getSlotsToMigrateCount()) {
                        int slot = src.getSlotList().get(0);
                        boolean result = RedisManager.migrate(src, des, slot);
                        if (result) {
                            reshardRecord.setSlotsDone(reshardRecord.getSlotsDone() + 1);
                            reshardService.updateReshardPlan(reshardPlan);
                        } else {
                            logger.error("Migrate slot : " + slot + " error.");
                            break;
                        }
                    } else {
                        break;
                    }
                } catch (Throwable e) {
                    logger.error("Some Error Occured In Migrating Task",e);
                    reshardPlan.setStatus(400);
                    reshardService.updateReshardPlan(reshardPlan);
                    break;
                }
            }
        }
    }


}
