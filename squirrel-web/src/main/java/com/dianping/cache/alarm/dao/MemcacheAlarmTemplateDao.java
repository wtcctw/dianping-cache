package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.MemcacheTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Transactional
public interface MemcacheAlarmTemplateDao  {

    /**
     * @param memcacheTemplate
     * @return
     */
    boolean insert(MemcacheTemplate memcacheTemplate);

    /**
     * @param memcacheTemplate
     * @return
     */
    boolean update(MemcacheTemplate memcacheTemplate);

    /**
     * @param id
     * @return
     */
    int deleteById(int id);

    /**
     * @param id
     * @return
     */
    MemcacheTemplate findById(int id);

    /**
     * @param templateName
     * @return
     */
    MemcacheTemplate findAlarmTemplateByTemplateName(String templateName);

    /**
     * @param
     * @return
     */
    List<MemcacheTemplate> findAll();

}
