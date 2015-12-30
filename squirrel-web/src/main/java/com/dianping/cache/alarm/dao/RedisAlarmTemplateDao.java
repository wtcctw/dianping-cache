package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Transactional
public interface RedisAlarmTemplateDao{

    /**
     * @param redisTemplate
     * @return
     */
    boolean insert(RedisTemplate redisTemplate);

    /**
     * @param redisTemplate
     * @return
     */
    boolean update(RedisTemplate redisTemplate);

    /**
     * @param id
     * @return
     */
    int deleteById(int id);

    /**
     * @param id
     * @return
     */
    RedisTemplate findById(int id);

    /**
     * @param templateName
     * @return
     */
    RedisTemplate findAlarmTemplateByTemplateName(String templateName);
    
    /**
     * @param
     * @return
     */
    List<RedisTemplate> findAll();

}
