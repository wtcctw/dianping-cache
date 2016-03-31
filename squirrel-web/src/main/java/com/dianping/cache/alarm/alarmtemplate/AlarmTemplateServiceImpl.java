package com.dianping.cache.alarm.alarmtemplate;

import com.dianping.cache.alarm.dao.AlarmTemplateDao;
import com.dianping.cache.alarm.entity.AlarmTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 16/3/15.
 */
@Service
public class AlarmTemplateServiceImpl implements AlarmTemplateService {

    @Autowired
    AlarmTemplateDao alarmTemplateDao;

    @Override
    public boolean insert(AlarmTemplate template) {
        return alarmTemplateDao.insert(template);
    }

    @Override
    public boolean update(AlarmTemplate template) {
        return alarmTemplateDao.update(template);
    }

    @Override
    public List<AlarmTemplate> findAll() {
        return alarmTemplateDao.findAll();
    }

    @Override
    public AlarmTemplate findById(int id) {
        return alarmTemplateDao.findById(id);
    }

    @Override
    public AlarmTemplate findAlarmTemplateByTemplateNameAndType(String templateName, String alarmType){
        return alarmTemplateDao.findAlarmTemplateByTemplateNameAndType(templateName, alarmType);
    }

    @Override
    public int deleteById(int id) {
        return alarmTemplateDao.deleteById(id);
    }
}
