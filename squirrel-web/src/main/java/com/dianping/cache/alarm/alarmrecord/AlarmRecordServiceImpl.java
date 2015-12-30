package com.dianping.cache.alarm.alarmrecord;

import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.entity.AlarmRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/11.
 */
@Service
public class AlarmRecordServiceImpl implements AlarmRecordService {

    @Autowired
    private AlarmRecordDao alarmRecordDao;

    @Override
    public boolean insert(AlarmRecord alarmRecord) {
        return alarmRecordDao.insert(alarmRecord);
    }

    @Override
    public List<AlarmRecord> findAll() {
        return alarmRecordDao.findAll();
    }

    @Override
    public List<AlarmRecord> findByPage(int offset, int limit) {
        return alarmRecordDao.findByPage(offset, limit);
    }

    @Override
    public List<AlarmRecord> findByType(int type) {
        return alarmRecordDao.findByType(type);
    }

    @Override
    public List<AlarmRecord> search(String sql) {
        return alarmRecordDao.search(sql);
    }
}
