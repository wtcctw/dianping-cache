package com.dianping.cache.alarm.redis;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.entity.AlarmRecord;
import com.dianping.cache.alarm.entity.RedisTemplate;
import com.dianping.cache.alarm.event.EventFactory;
import com.dianping.cache.alarm.event.EventType;
import com.dianping.cache.alarm.event.EventReporter;
import com.dianping.cache.controller.RedisDataUtil;
import com.dianping.squirrel.view.highcharts.statsdata.RedisClusterData;
import com.dianping.cache.scale.cluster.redis.RedisNode;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private static final String MASTER_SLAVE_DIFF = "Master和Slave数量不一致";

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
    RedisBaselineService redisBaselineService;

    @Override
    public void doAlarm() throws InterruptedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException, IOException, TimeoutException {

        RedisEvent redisEvent = eventFactory.createRedisEvent();


        List<RedisClusterData> redisClusterDatas = RedisDataUtil.getClusterData();

        boolean isReport = false;


        for (RedisClusterData item : redisClusterDatas) {
            AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

            if ((null == alarmConfig)&&(null!=item.getClusterName())) {
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
                            .setAlarmDetail(item.getClusterName() + ":" + redisServer.getAddress() + ";" + CLUSTER_DOWN)
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

            //主从数量不一致告警
            if(item.getMasterNum()!= item.getSlaveNum()){
                AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                isReport = true;
                alarmDetail.setAlarmTitle(MASTER_SLAVE_DIFF)
                        .setAlarmDetail(item.getClusterName() + ":"  + MASTER_SLAVE_DIFF)
                        .setMailMode(redisTemplate.isMailMode())
                        .setSmsMode(redisTemplate.isSmsMode())
                        .setWeixinMode(redisTemplate.isWeixinMode())
                        .setCreateTime(new Date());

                AlarmRecord alarmRecord = new AlarmRecord();
                alarmRecord.setAlarmTitle(MASTER_SLAVE_DIFF)
                        .setClusterName(item.getClusterName())
                        .setIp(null)
                        .setCreateTime(new Date());

                alarmRecordDao.insert(alarmRecord);

                redisEvent.put(alarmDetail);
            }


            for (RedisNode node : item.getNodes()) {

                //Mem
                //Master
                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                    continue;
                }
                if (node.getMaster().getInfo().getUsed() > redisTemplate.getMemThreshold()) {
                    isReport = true;
                    String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + MEMUSAGE_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getUsed();
                    putToChannel(alarmConfig, MEMUSAGE_TOO_HIGH, item, redisTemplate, node, detail, redisEvent, node.getMaster().getInfo().getQps());

                }


                //QPS
                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                    continue;
                }
                if (node.getMaster().getInfo().getQps() > redisTemplate.getQpsThreshold()) {
                    isReport = true;
                    String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + QPS_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getQps();
                    putToChannel(alarmConfig, QPS_TOO_HIGH, item, redisTemplate, node, detail, redisEvent, node.getMaster().getInfo().getQps());

                }

                if (redisTemplate.isCheckHistory()) {//是否进行历史数据分析开关

                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm", Locale.ENGLISH);
                    Date nameDate = new Date();
                    String name = "Redis_" + sdf.format(nameDate) + "_" + node.getMaster().getAddress();


                    //total_connections_received
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }

                    if(0==redisBaselineService.findByName(name).size()){
                        continue;
                    }

                    if (fluctTooMuch((double) node.getMaster().getInfo().getTotal_connections(), (double) redisBaselineService.findByName(name).get(0).getTotal_connections())) {

                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + TOTAL_CONNECTIONS;
                        putToChannel(alarmConfig, TOTAL_CONNECTIONS, item, redisTemplate, node, detail, redisEvent, null);

                    }

                    //connected_clients
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }


                    if (fluctTooMuch((double) node.getMaster().getInfo().getConnected_clients(), (double) redisBaselineService.findByName(name).get(0).getConnected_clients())) {

                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + CONNECTED_CLIENTS;
                        putToChannel(alarmConfig, CONNECTED_CLIENTS, item, redisTemplate, node, detail, redisEvent, null);

                    }


                    //input_kbps
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }


                    if (fluctTooMuch((double) node.getMaster().getInfo().getInput_kbps(), (double) redisBaselineService.findByName(name).get(0).getInput_kbps())) {

                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + INPUT_KBPS;
                        putToChannel(alarmConfig, INPUT_KBPS, item, redisTemplate, node, detail, redisEvent, null);

                    }

                    //output_kbps
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }


                    if (fluctTooMuch((double) node.getMaster().getInfo().getOutput_kbps(), (double) redisBaselineService.findByName(name).get(0).getOutput_kbps())) {

                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + OUTPUT_KBPS;
                        putToChannel(alarmConfig, OUTPUT_KBPS, item, redisTemplate, node, detail, redisEvent, null);


                    }

                    //used_cpu_sys
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }


                    if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_sys(), (double) redisBaselineService.findByName(name).get(0).getUsed_cpu_sys())) {
                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_SYS;
                        putToChannel(alarmConfig, USED_CPU_SYS, item, redisTemplate, node, detail, redisEvent, null);

                    }


                    //used_cpu_user
                    if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                        continue;
                    }


                    if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_user(), (double) redisBaselineService.findByName(name).get(0).getUsed_cpu_user())) {

                        isReport = true;
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_USER;
                        putToChannel(alarmConfig, USED_CPU_USER, item, redisTemplate, node, detail, redisEvent, null);

                    }

                }
            }
        }

        if (isReport) {
            redisEvent.setEventType(EventType.REDIS).setCreateTime(new Date());

            eventReporter.report(redisEvent);

        }

    }

    private void putToChannel(AlarmConfig alarmConfig, String type, RedisClusterData item, RedisTemplate redisTemplate, RedisNode node, String detail,RedisEvent redisEvent, Object o) {

        AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

        alarmDetail.setAlarmTitle(item.getClusterName() + type)
                .setAlarmDetail(detail)
                .setMailMode(redisTemplate.isMailMode())
                .setSmsMode(redisTemplate.isSmsMode())
                .setWeixinMode(redisTemplate.isWeixinMode())
                .setCreateTime(new Date());

        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setAlarmTitle(type)
                .setClusterName(item.getClusterName())
                .setIp(node.getMaster().getAddress())
                .setCreateTime(new Date());

        alarmRecordDao.insert(alarmRecord);

        redisEvent.put(alarmDetail);

    }


    private boolean fluctTooMuch(double cur, double base) {
        boolean result = false;

        if(0 == base){
            return result;
        }

        if (Math.abs((cur - base)) / base > 0.5) {
            result = true;
        }

        return result;
    }

}
