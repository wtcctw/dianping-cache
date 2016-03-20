package com.dianping.cache.alarm.report.scanService;


import com.dianping.cache.alarm.entity.ScanStatistics;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/13.
 */
public interface ScanStatisticsService {

    void insert(ScanStatistics scanStatistics);

    List<ScanStatistics> findAll();

    List<ScanStatistics> findByCreateTime(String createTime);

    List<ScanStatistics>findByPage(int offset, int limit);

    List<ScanStatistics> search(String sql);

}
