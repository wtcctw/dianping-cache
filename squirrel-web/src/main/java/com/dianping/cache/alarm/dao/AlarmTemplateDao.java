package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.AlarmTemplate;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Transactional
public interface AlarmTemplateDao {

    /**
     * @param template
     * @return
     */
    boolean insert(AlarmTemplate template);

    /**
     * @param template
     * @return
     */
    boolean update(AlarmTemplate template);

    /**
     * @param id
     * @return
     */
    int deleteById(int id);

    /**
     * @param id
     * @return
     */
    AlarmTemplate findById(int id);

    /**
     * @param templateName,alarmType
     * @return
     */
    AlarmTemplate findAlarmTemplateByTemplateNameAndType(@Param("templateName")String templateName, @Param("alarmType")String alarmType);

    /**
     * @param
     * @return
     */
    List<AlarmTemplate> findAll();

}
