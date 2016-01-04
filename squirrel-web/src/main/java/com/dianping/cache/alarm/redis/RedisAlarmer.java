package com.dianping.cache.alarm.redis;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.entity.AlarmRecord;
import com.dianping.cache.alarm.entity.RedisTemplate;
import com.dianping.cache.alarm.event.EventFactory;
import com.dianping.cache.alarm.event.EventType;
import com.dianping.cache.alarm.report.EventReporter;
import com.dianping.cache.controller.RedisDashBoardUtil;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.scale.cluster.redis.RedisNode;
import com.dianping.cache.scale.cluster.redis.RedisServer;
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

    @Autowired
    RedisAlarmTemplateService redisAlarmTemplateService;

    @Override
    public void doAlarm() throws InterruptedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException, IOException, TimeoutException {

        RedisEvent redisEvent = eventFactory.createRedisEvent();


        List<RedisClusterData> redisClusterDatas = RedisDashBoardUtil.getClusterData();

        boolean isReport = false;


        for (RedisClusterData item : redisClusterDatas) {
            AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

            if (null == alarmConfig) {
                alarmConfig = new AlarmConfig("Redis", item.getClusterName());
                alarmConfigService.insert(alarmConfig);
            }

            RedisTemplate redisTemplate = redisAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());


            //cluster down
            if (redisTemplate.isDown()) {
                for (RedisServer redisServer : item.getFailedServers()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(CLUSTER_DOWN)
                            .setAlarmDetail(item.getClusterName() + ":" + CLUSTER_DOWN + ";IP为" + redisServer.getIp())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_CLUSTER_DOWN.getNumber())
                            .setAlarmTitle(CLUSTER_DOWN)
                            .setClusterName(item.getClusterName())
                            .setIp(redisServer.getIp())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }
            }


            for (RedisNode node : item.getNodes()) {

                //Mem
                //Master
                if (node.getMaster().getInfo().getUsed() > redisTemplate.getMemThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setAlarmDetail("Master:" + node.getMaster().getIp() + ":" + MEMUSAGE_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getUsed())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_MEMUSAGE_TOO_HIGH.getNumber())
                            .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setClusterName(item.getClusterName() + ":Master")
                            .setIp(node.getMaster().getIp())
                            .setValue(node.getMaster().getInfo().getUsed())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }
                //Slave
                if (node.getSlave().getInfo().getUsed() > redisTemplate.getMemThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setAlarmDetail("Slave:" + node.getSlave().getIp() + ":" + MEMUSAGE_TOO_HIGH + ";使用率为" + node.getSlave().getInfo().getUsed())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_MEMUSAGE_TOO_HIGH.getNumber())
                            .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setClusterName(item.getClusterName() + ":Slave")
                            .setIp(node.getSlave().getIp())
                            .setValue(node.getSlave().getInfo().getUsed())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }

                //QPS
                if (node.getMaster().getInfo().getQps() > redisTemplate.getQpsThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(QPS_TOO_HIGH)
                            .setAlarmDetail("Master:" + node.getMaster().getIp() + ":" + QPS_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getQps())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_QPS_TOO_HIGH.getNumber())
                            .setAlarmTitle(QPS_TOO_HIGH)
                            .setClusterName(item.getClusterName() + ":Master")
                            .setIp(node.getMaster().getIp())
                            .setValue(node.getMaster().getInfo().getQps())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }
                //Slave
                if (node.getSlave().getInfo().getQps() > redisTemplate.getQpsThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(QPS_TOO_HIGH)
                            .setAlarmDetail("Slave:" + node.getSlave().getIp() + ":" + QPS_TOO_HIGH + ";使用率为" + node.getSlave().getInfo().getUsed())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_MEMUSAGE_TOO_HIGH.getNumber())
                            .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setClusterName(item.getClusterName() + ":Slave")
                            .setIp(node.getSlave().getIp())
                            .setValue(node.getSlave().getInfo().getQps())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }
            }
        }

        if (isReport) {
            redisEvent.setEventType(EventType.REDIS).setCreateTime(new Date());

            eventReporter.report(redisEvent);

        }

    }

}
