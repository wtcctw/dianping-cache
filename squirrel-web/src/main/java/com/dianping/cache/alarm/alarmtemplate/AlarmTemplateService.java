package com.dianping.cache.alarm.alarmtemplate;

import com.dianping.cache.alarm.entity.AlarmTemplate;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/24.
 */
public interface AlarmTemplateService {

    boolean insert(AlarmTemplate template);

    boolean update(AlarmTemplate template);

    List<AlarmTemplate> findAll();

    AlarmTemplate findById(int id);

    AlarmTemplate findAlarmTemplateByTemplateNameAndType(String templateName, String alarmType);

    int deleteById(int id);

}
