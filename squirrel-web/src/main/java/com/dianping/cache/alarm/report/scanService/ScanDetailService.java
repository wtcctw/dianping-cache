package com.dianping.cache.alarm.report.scanService;


import com.dianping.cache.alarm.entity.ScanDetail;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/13.
 */
public interface ScanDetailService {

    void insert(ScanDetail scanDetail);

    List<ScanDetail> findAll();

    List<ScanDetail> findByCreateTime(String createTime);

    List<ScanDetail>findByPage( int offset,int limit);

    List<ScanDetail> search(String sql);

}
