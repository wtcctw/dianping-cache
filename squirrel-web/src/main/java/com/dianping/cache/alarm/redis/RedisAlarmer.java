package com.dianping.cache.alarm.redis;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.dataanalyse.baselineCache.BaselineCacheService;
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

    private static final String TOTAL_CONNECTIONS = "total_connections_received波动过大";
    private static final String CONNECTED_CLIENTS = "connected_clients波动过大";
    private static final String INPUT_KBPS = "input_kbps波动过大";
    private static final String OUTPUT_KBPS = "output_kbps波动过大";
    private static final String USED_CPU_SYS = "used_cpu_sys波动过大";
    private static final String USED_CPU_USER = "used_cpu_user波动过大";

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

    @Autowired
    BaselineCacheService baselineCacheService;

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
                            .setAlarmDetail(item.getClusterName() + ":"+ redisServer.getAddress() + ";" + CLUSTER_DOWN )
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_CLUSTER_DOWN.getNumber())
                            .setAlarmTitle(CLUSTER_DOWN)
                            .setClusterName(item.getClusterName())
                            .setIp(redisServer.getAddress())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }
            }


            for (RedisNode node : item.getNodes()) {

                //Mem
                //Master
                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                    continue;
                }
                if (node.getMaster().getInfo().getUsed() > redisTemplate.getMemThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + MEMUSAGE_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getUsed())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_MEMUSAGE_TOO_HIGH.getNumber())
                            .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                            .setClusterName(item.getClusterName())
                            .setIp(node.getMaster().getAddress())
                            .setValue(node.getMaster().getInfo().getUsed())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }


                //QPS
                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                    continue;
                }
                if (node.getMaster().getInfo().getQps() > redisTemplate.getQpsThreshold()) {
                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                    isReport = true;
                    alarmDetail.setAlarmTitle(QPS_TOO_HIGH)
                            .setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + QPS_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getQps())
                            .setMailMode(redisTemplate.isMailMode())
                            .setSmsMode(redisTemplate.isSmsMode())
                            .setWeixinMode(redisTemplate.isWeixinMode())
                            .setCreateTime(new Date());

                    AlarmRecord alarmRecord = new AlarmRecord();
                    alarmRecord.setAlarmType(AlarmType.REDIS_QPS_TOO_HIGH.getNumber())
                            .setAlarmTitle(QPS_TOO_HIGH)
                            .setClusterName(item.getClusterName())
                            .setIp(node.getMaster().getAddress())
                            .setValue(node.getMaster().getInfo().getQps())
                            .setCreateTime(new Date());

                    alarmRecordDao.insert(alarmRecord);

                    redisEvent.put(alarmDetail);
                }

//                SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm");
//                Date nameDate = new Date();
//                String name = "Redis_" + sdf.format(nameDate);
//
//
//                //total_connections_received
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getTotal_connections(), (double) baselineCacheService.getRedisBaselineByName(name).getTotal_connections())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + TOTAL_CONNECTIONS)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(TOTAL_CONNECTIONS)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }
//
//                //connected_clients
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getConnected_clients(), (double) baselineCacheService.getRedisBaselineByName(name).getConnected_clients())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + CONNECTED_CLIENTS)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(CONNECTED_CLIENTS)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }
//
//
//                //input_kbps
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getInput_kbps(), (double) baselineCacheService.getRedisBaselineByName(name).getInput_kbps())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + INPUT_KBPS)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(INPUT_KBPS)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }
//
//                //output_kbps
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getOutput_kbps(), (double) baselineCacheService.getRedisBaselineByName(name).getOutput_kbps())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + OUTPUT_KBPS)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(OUTPUT_KBPS)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }
//
//                //used_cpu_sys
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_sys(), (double) baselineCacheService.getRedisBaselineByName(name).getUsed_cpu_sys())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_SYS)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(USED_CPU_SYS)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }
//
//
//                //used_cpu_user
//                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
//                    continue;
//                }
//
//
//                if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_user(), (double) baselineCacheService.getRedisBaselineByName(name).getUsed_cpu_user())) {
//                    AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
//                    isReport = true;
//                    alarmDetail.setAlarmDetail(item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_USER)
//                            .setMailMode(redisTemplate.isMailMode())
//                            .setSmsMode(redisTemplate.isSmsMode())
//                            .setWeixinMode(redisTemplate.isWeixinMode())
//                            .setCreateTime(new Date());
//
//                    AlarmRecord alarmRecord = new AlarmRecord();
//                    alarmRecord.setAlarmTitle(USED_CPU_USER)
//                            .setClusterName(item.getClusterName())
//                            .setIp(node.getMaster().getAddress())
//                            .setCreateTime(new Date());
//
//                    alarmRecordDao.insert(alarmRecord);
//
//                    redisEvent.put(alarmDetail);
//                }


            }
        }

        if (isReport) {
            redisEvent.setEventType(EventType.REDIS).setCreateTime(new Date());

            eventReporter.report(redisEvent);

        }

    }

    private boolean fluctTooMuch(double v1, double v2) {
        boolean result = false;

        if (Math.abs((v1 - v2)) / v2 > 0.5) {
            result = true;
        }

        return result;
    }

}
