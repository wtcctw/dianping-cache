package com.dianping.cache.alarm.report.scanService;

import com.dianping.cache.alarm.dao.ScanStatisticsDao;
import com.dianping.cache.alarm.entity.ScanStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/13.
 */
@Service
public class ScanStatisticsServiceImpl implements ScanStatisticsService {

    @Autowired
    ScanStatisticsDao scanStatisticsDao;

    @Override
    public void insert(ScanStatistics scanStatistics) {
        scanStatisticsDao.insert(scanStatistics);
    }

    @Override
    public List<ScanStatistics> findAll() {
        return scanStatisticsDao.findAll();
    }

    @Override
    public List<ScanStatistics> findByCreateTime(String createTime) {
        return scanStatisticsDao.findByCreateTime(createTime);
    }

    @Override
    public List<ScanStatistics> findByPage(int offset, int limit) {
        return scanStatisticsDao.findByPage(offset,limit);
    }

    @Override
    public List<ScanStatistics> search(String sql) {
        return scanStatisticsDao.search(sql);
    }
}
