package com.dianping.cache.deamontask.dao;

import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.entity.DeamonTask;

/**
 * Created by thunder on 16/1/6.
 */
public interface DeamonTaskDao {
    boolean insert(DeamonTask task);
}
