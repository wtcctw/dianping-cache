package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.ScanStatistics;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/11/22.
 */
@Transactional
public interface ScanStatisticsDao {

    /**
     * @param scanStatistics
     * @return
     */
    boolean insert(ScanStatistics scanStatistics);

    /**
     * retrieve all ScanDetail
     * @return
     */
    List<ScanStatistics> findAll();

    /**
     * find day detail
     *
     * @param
     * @return
     */
    List<ScanStatistics> findByCreateTime(String createTime);

    /**
     * @param offset,limit
     * @return
     */
    List<ScanStatistics>findByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * @param sql
     * @return
     */
    List<ScanStatistics> search(@Param("paramSQL") String sql);

}
