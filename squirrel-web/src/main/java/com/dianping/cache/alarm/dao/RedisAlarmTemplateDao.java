package com.dianping.cache.alarm.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.cache.alarm.entity.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Transactional
public interface RedisAlarmTemplateDao extends GenericDao {

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
     * @param clusterName
     * @return
     */
    RedisTemplate findAlarmTemplateByClusterName(String clusterName);
    
    /**
     * @param
     * @return
     */
    List<RedisTemplate> findAll();

}
