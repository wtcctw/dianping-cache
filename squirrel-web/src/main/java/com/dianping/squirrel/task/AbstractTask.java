package com.dianping.squirrel.task;

import com.dianping.cache.util.RequestUtil;
import com.dianping.cache.util.SpringLocator;
import com.dianping.squirrel.dao.TaskDao;
import com.dianping.squirrel.entity.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thunder on 16/1/5.
 */
public abstract class AbstractTask<T> implements Runnable{

    public int getTaskId() {
        return this.task.getId();
    }

    protected Task task;

    abstract String getTaskType();
    abstract int getTaskMinStat();
    abstract long getTaskMaxStat();

    private TaskDao taskDao = SpringLocator.getBean("taskDao");

    public void startTask() {
        Task task = new Task();
        task.setCommitTime(System.currentTimeMillis());
        task.setType(getTaskType());
        task.setStatMax((int)getTaskMaxStat());
        task.setCommiter(RequestUtil.getUsername());
        task.setStatMin(getTaskMinStat());
        this.task = task;
        taskDao.insert(task);
    }

    protected void endTask() {
        Task task = this.task;
        Map<String, String> para = new HashMap<String, String>();
        para.put("id", Integer.toString(task.getId()));
        para.put("endTime", Long.toString(System.currentTimeMillis()));
        taskDao.updateEndTime(para);
    }

    public void updateStat(int stat) {
        Task task = this.task;
        Map<String, String> para = new HashMap<String, String>();
        para.put("id", Integer.toString(task.getId()));
        para.put("stat", Integer.toString(stat));
        taskDao.updateStat(para);
    }

    public abstract void taskRun();

    public void run() {
        //startTask();
        taskRun();
        endTask();
    }

}
