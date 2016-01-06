package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.dao.MemcacheBaselineDao;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/4.
 */
@Component
public class MemcacheBaselineServiceImpl implements MemcacheBaselineService {

    @Autowired
    MemcacheBaselineDao memcacheBaselineDao;

    @Override
    public List<MemcacheBaseline> findAll() {
        return memcacheBaselineDao.findAll();
    }

    @Override
    public List<MemcacheBaseline> findByTaskId(int taskId) {
        return memcacheBaselineDao.findByTaskId(taskId);
    }

    @Override
    public List<MemcacheBaseline> findByServer(String server) {
        return memcacheBaselineDao.findByServer(server);
    }

    @Override
    public void insert(MemcacheBaseline stat) {
        memcacheBaselineDao.insert(stat);

    }

    @Override
    public List<MemcacheBaseline> findByServerWithInterval(String address, long start, long end) {
        return memcacheBaselineDao.findByServerWithInterval(address,start,end);
    }

    @Override
    public void delete(long timeBefore) {
        memcacheBaselineDao.delete(timeBefore);
    }

    @Override
    public List<MemcacheBaseline> search(String sql) {
        return memcacheBaselineDao.search(sql);
    }
}
