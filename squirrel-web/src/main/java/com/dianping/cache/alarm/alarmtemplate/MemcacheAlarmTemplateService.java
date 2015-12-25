package com.dianping.cache.alarm.alarmtemplate;

import com.dianping.cache.alarm.entity.MemcacheTemplate;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/24.
 */
public interface MemcacheAlarmTemplateService {

    boolean insert(MemcacheTemplate memcacheTemplate);

    boolean update(MemcacheTemplate memcacheTemplate);

    List<MemcacheTemplate> findAll();

    MemcacheTemplate findById(int id);

    MemcacheTemplate findAlarmTemplateByTemplateName(String clusterName);

    int deleteById(int id);

}
