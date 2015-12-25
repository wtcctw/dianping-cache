package com.dianping.cache.alarm.alarmtemplate;

import com.dianping.cache.alarm.dao.RedisAlarmTemplateDao;
import com.dianping.cache.alarm.entity.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/24.
 */
@Service
public class RedisAlarmTemplateServiceImpl implements RedisAlarmTemplateService {

    @Autowired
    RedisAlarmTemplateDao redisAlarmTemplateDao;

    @Override
    public boolean insert(RedisTemplate redisTemplate) {
        return redisAlarmTemplateDao.insert(redisTemplate);
    }

    @Override
    public boolean update(RedisTemplate memcacheTemplate) {
        return redisAlarmTemplateDao.update(memcacheTemplate);
    }

    @Override
    public List<RedisTemplate> findAll() {
        return redisAlarmTemplateDao.findAll();
    }

    @Override
    public RedisTemplate findById(int id) {
        return redisAlarmTemplateDao.findById(id);
    }

    @Override
    public RedisTemplate findAlarmTemplateByClusterName(String templateName) {
        return redisAlarmTemplateDao.findAlarmTemplateByTemplateName(templateName);
    }

    @Override
    public int deleteById(int id) {
        return redisAlarmTemplateDao.deleteById(id);
    }
}
