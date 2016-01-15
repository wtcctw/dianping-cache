package com.dianping.cache.alarm.dao;

import com.dianping.cache.alarm.entity.ScanDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by lvshiyun on 15/11/22.
 */
@Transactional
public interface ScanDetailDao {

    /**
     * @param detail
     * @return
     */
    boolean insert(ScanDetail detail);

    /**
     * retrieve all ScanDetail
     * @return
     */
    List<ScanDetail> findAll();

    /**
     * find day detail
     *
     * @param
     * @return
     */
    List<ScanDetail> findByCreateTime(String createTime);

    /**
     * @param offset,limit
     * @return
     */
    List<ScanDetail>findByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * @param sql
     * @return
     */
    List<ScanDetail> search(@Param("paramSQL") String sql);

}
