package com.dianping.cache.alarm.redis;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.entity.*;
import com.dianping.cache.alarm.event.EventFactory;
import com.dianping.cache.alarm.event.EventType;

import com.dianping.cache.alarm.report.EventReporter;
import com.dianping.cache.controller.RedisDashBoardUtil;
;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by lvshiyun on 15/11/21.
 */
@Service
public class RedisAlarmer extends AbstractRedisAlarmer {

    private static final String CLUSTER_DOWN = "集群实例无法连接";
    private static final String MEMUSAGE_TOO_HIGH = "内存使用率过高";
    private static final String QPS_TOO_HIGH = "QPS过高";
    private static final String CONN_TOO_HIGH = "连接数过高";

    private static final String ALARMTYPE = "Redis";

    @Autowired
    protected EventFactory eventFactory;

    @Autowired
    protected EventReporter eventReporter;

    @Autowired
    AlarmRecordDao alarmRecordDao;

    @Autowired
    AlarmConfigService alarmConfigService;


    @Override
    public void doAlarm() throws InterruptedException, MemcachedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException,IOException, TimeoutException {

        RedisEvent redisEvent = eventFactory.createRedisEvent();


        List<RedisClusterData> redisClusterDatas = RedisDashBoardUtil.getClusterData();

        boolean isReport = false;


        for (RedisClusterData item : redisClusterDatas) {
            AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

            if(null == alarmConfig){
                continue;
            }

            if (item.getUsed()> alarmConfig.getThreshold()) {
                AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                isReport = true;
                alarmDetail.setAlarmTitle(MEMUSAGE_TOO_HIGH)
                        .setAlarmDetail(item.getClusterName() + ":" + MEMUSAGE_TOO_HIGH + ";使用率为" + item.getUsed())
                        .setCreateTime(new Date());

                AlarmRecord alarmRecord = new AlarmRecord();
                alarmRecord.setAlarmType(AlarmType.REDIS_MEMUSAGE_TOO_HIGH.getNumber())
                        .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                        .setAlarmDetail(item.getClusterName() + ":" + MEMUSAGE_TOO_HIGH + ";使用率为" + item.getUsed())
                        .setCreateTime(new Date());

                alarmRecordDao.insert(alarmRecord);

                redisEvent.put(alarmDetail);
            }
        }

        if (isReport) {
            redisEvent.setEventType(EventType.REDIS).setCreateTime(new Date());

            eventReporter.report(redisEvent);

        }

    }

}
