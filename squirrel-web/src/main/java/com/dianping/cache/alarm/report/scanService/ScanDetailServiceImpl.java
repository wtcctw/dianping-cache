package com.dianping.cache.alarm.report.scanService;

import com.dianping.cache.alarm.dao.ScanDetailDao;
import com.dianping.cache.alarm.entity.ScanDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/13.
 */
@Service
public class ScanDetailServiceImpl implements ScanDetailService {

    @Autowired
    ScanDetailDao scanDetailDao;

    @Override
    public void insert(ScanDetail scanDetail) {
        scanDetailDao.insert(scanDetail);
    }

    @Override
    public List<ScanDetail> findAll() {
        return scanDetailDao.findAll();
    }

    @Override
    public List<ScanDetail> findByCreateTime(String createTime) {
        return scanDetailDao.findByCreateTime(createTime);
    }

    @Override
    public List<ScanDetail> findByPage(int offset, int limit) {
        return scanDetailDao.findByPage(offset,limit);
    }

    @Override
    public List<ScanDetail> search(String sql) {
        return scanDetailDao.search(sql);
    }
}
