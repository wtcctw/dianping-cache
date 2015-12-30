package com.dianping.cache.alarm.alarmconfig;

import com.dianping.cache.alarm.dao.AlarmConfigDao;
import com.dianping.cache.alarm.entity.AlarmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/18.
 */
@Service
public class AlarmConfigServiceImpl implements AlarmConfigService {

    @Autowired
    AlarmConfigDao alarmConfigDao;

    @Override
    public boolean insert(AlarmConfig alarmConfig) {
        return alarmConfigDao.insert(alarmConfig);
    }

    @Override
    public boolean update(AlarmConfig alarmConfig) {
        return alarmConfigDao.update(alarmConfig);
    }

    @Override
    public int deleteById(int id) {
        return alarmConfigDao.deleteById(id);
    }

    @Override
    public AlarmConfig findByClusterTypeAndName(String clusterType, String clusterName) {
        return alarmConfigDao.findByClusterTypeAndName(clusterType, clusterName);
    }

    @Override
    public AlarmConfig findById(int id) {
        return alarmConfigDao.findById(id);
    }


    @Override
    public List<AlarmConfig> findByPage(int offset, int limit) {
        return alarmConfigDao.findByPage(offset,limit);
    }

    @Override
    public List<AlarmConfig> findAll() {
        return alarmConfigDao.findAll();
    }


}
