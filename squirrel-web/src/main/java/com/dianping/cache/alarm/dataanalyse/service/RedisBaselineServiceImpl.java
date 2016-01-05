package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.dao.RedisBaselineDao;
import com.dianping.cache.alarm.entity.RedisBaseline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/4.
 */
@Component
public class RedisBaselineServiceImpl implements RedisBaselineService {
    @Autowired
    RedisBaselineDao redisBaselineDao;

    @Override
    public List<RedisBaseline> findAll() {
        return redisBaselineDao.findAll();
    }

    @Override
    public List<RedisBaseline> findByServer(String server) {
        return redisBaselineDao.findByServer(server);
    }

    @Override
    public void insert(RedisBaseline stat) {
        redisBaselineDao.insert(stat);

    }

    @Override
    public List<RedisBaseline> findByServerWithInterval(String address, long start, long end) {
        return redisBaselineDao.findByServerWithInterval(address,start,end);
    }

    @Override
    public void delete(long timeBefore) {
        redisBaselineDao.delete(timeBefore);
    }

    @Override
    public List<RedisBaseline> search(String sql) {
        return redisBaselineDao.search(sql);
    }
}
