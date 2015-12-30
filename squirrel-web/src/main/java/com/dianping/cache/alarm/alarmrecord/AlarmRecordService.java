package com.dianping.cache.alarm.alarmrecord;
import com.dianping.cache.alarm.entity.AlarmRecord;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
public interface AlarmRecordService {
    boolean insert(AlarmRecord alarmRecord);

    List<AlarmRecord> findAll();

    List<AlarmRecord>findByPage(int offset, int limit);

    List<AlarmRecord>findByType(int type);

    List<AlarmRecord>search(String sql);
}
