package com.dianping.squirrel.dao;

import com.dianping.squirrel.entity.Task;

import java.util.List;
import java.util.Map;

/**
 * Created by thunder on 16/1/6.
 */
public interface TaskDao {
    
    boolean insert(Task task);

    List<Task> selectAll();

    boolean updateStat(Map<String, String> map);

    Task getTask(int id);

    boolean updateStartTime(Map<String ,String> map);
    boolean updateEndTime(Map<String ,String> map);
    boolean cancelTask(int id);
}
