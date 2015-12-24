package com.dianping.cache.alarm.alarmtemplate;

import com.dianping.cache.alarm.entity.RedisTemplate;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/24.
 */
public interface RedisAlarmTemplateService {

    boolean insert(RedisTemplate redisTemplate);

    boolean update(RedisTemplate redisTemplate);

    List<RedisTemplate> findAll();

    RedisTemplate findById(int id);

    RedisTemplate findAlarmTemplateByClusterName(String clusterName);

    int deleteById(int id);

}
