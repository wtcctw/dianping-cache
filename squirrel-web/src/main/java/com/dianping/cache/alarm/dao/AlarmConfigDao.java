package com.dianping.cache.alarm.dao;

import com.dianping.avatar.dao.GenericDao;
import com.dianping.cache.alarm.entity.AlarmConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
@Transactional
public interface AlarmConfigDao extends GenericDao {

    /**
     * @param alarmConfig
     * @return
     */
    boolean insert(AlarmConfig alarmConfig);

    /**
     * @param alarmConfig
     * @return
     */
    boolean update(AlarmConfig alarmConfig);

    /**
     * @param id
     * @return
     */
    int deleteById(int id);

    /**
     * @param clusterType,clusterName
     * @return
     */
    AlarmConfig findByClusterTypeAndName(@Param("clusterType") String clusterType, @Param("clusterName") String clusterName);

    /**
     * @param id
     * @return
     */
    AlarmConfig findById(int id);

    /**
     * @param offset,limit
     * @return
     */
    List<AlarmConfig> findByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * @param
     * @return
     */
    List<AlarmConfig> findAll();

}
