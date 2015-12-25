package com.dianping.cache.alarm.alarmconfig;

import com.dianping.cache.alarm.entity.AlarmConfig;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
public interface AlarmConfigService {
    boolean insert(AlarmConfig alarmConfig);

    boolean update(AlarmConfig alarmConfig);

    int deleteById(int id);

    AlarmConfig findByClusterTypeAndName(String clusterType, String clusterName);

    AlarmConfig findById(int id);

    List<AlarmConfig> findByPage(int offset, int limit);

    List<AlarmConfig> findAll();
}
