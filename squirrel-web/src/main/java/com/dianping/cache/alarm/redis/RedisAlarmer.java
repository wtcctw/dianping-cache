package com.dianping.cache.alarm.redis;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.RedisAlarmTemplateService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.dataanalyse.baselineCache.BaselineCacheService;
import com.dianping.cache.alarm.dataanalyse.minValCache.MinVal;
import com.dianping.cache.alarm.dataanalyse.minValCache.MinValCacheService;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.*;
import com.dianping.cache.alarm.event.EventFactory;
import com.dianping.cache.alarm.event.EventReporter;
import com.dianping.cache.alarm.event.EventType;
import com.dianping.cache.controller.RedisDataUtil;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.cluster.redis.RedisNode;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import com.dianping.cache.service.ServerService;
import com.dianping.squirrel.view.highcharts.statsdata.RedisClusterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Created by lvshiyun on 15/11/21.
 */
@Service
public class RedisAlarmer extends AbstractRedisAlarmer {

    private static final int MEMUSAGE = 0;
    private static final int QPS = 1;

    private static final String CLUSTER_DOWN = "集群实例无法连接";
    private static final String MEMUSAGE_TOO_HIGH = "内存使用率过高";
    private static final String MEMUSAGE_INCREASE_TOO_MUCH = "内存增长过快";
    private static final String QPS_TOO_HIGH = "QPS过高";
    private static final String QPS_INCREASE_TOO_MUCH = "QPS增长过快";
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

    @Autowired
    RedisStatsFlucService redisStatsFlucService;

    @Autowired
    private ServerService serverService;

    @Autowired
    BaselineCacheService baselineCacheService;

    @Autowired
    MinValCacheService minValCacheService;


    @Override
    public void doAlarm() throws InterruptedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException, IOException, TimeoutException {

        //创建告警事件
        RedisEvent redisEvent = eventFactory.createRedisEvent();

        List<RedisClusterData> redisClusterDatas = RedisDataUtil.getClusterData();

        boolean isReport = false;

        for (RedisClusterData item : redisClusterDatas) {
            AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

            if ((null == alarmConfig) && (null != item.getClusterName())) {
                alarmConfig = new AlarmConfig("Redis", item.getClusterName());
                alarmConfigService.insert(alarmConfig);
            }

            RedisTemplate redisTemplate = redisAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());


            boolean isDownAlarm = isDownAlarm(item, redisClusterDatas, redisEvent);

            boolean masterSlaveConsistency = masterSlaveConsistency(item, redisEvent);

            //内存使用率和QPS
            for (RedisNode node : item.getNodes()) {
                //Master
                if (null == node.getMaster() || null == node.getMaster().getInfo()) {
                    continue;
                }
                boolean isMemAlarm = isMemAlarm(item, node, alarmConfig, redisTemplate, redisEvent);


                boolean isMemFlucAlarm = isMemFlucAlarm(item, node, alarmConfig, redisTemplate, redisEvent);


                boolean isQpsAlarm = isQpsAlarm(item, node, alarmConfig, redisTemplate, redisEvent);

                boolean isQpsFlucAlarm = isQpsFlucAlarm(item, node, alarmConfig, redisTemplate, redisEvent);

                boolean isHistoryAlarm = false;
                if (redisTemplate.isCheckHistory()) {//是否进行历史数据分析开关
                    isHistoryAlarm = isHistoryAlarm(item, node, alarmConfig, redisTemplate, redisEvent);
                }

                if ((false == isReport) && (isMemAlarm || isMemFlucAlarm || isQpsAlarm || isQpsFlucAlarm || isHistoryAlarm)) {
                    isReport = true;
                }
            }
            if ((false == isReport) && (isDownAlarm || masterSlaveConsistency)) {
                isReport = true;
            }
        }


        if (isReport) {
            redisEvent.setEventType(EventType.REDIS).setCreateTime(new Date());

            eventReporter.report(redisEvent);

        }

    }

    private boolean isHistoryAlarm(RedisClusterData item, RedisNode node, AlarmConfig alarmConfig, RedisTemplate redisTemplate, RedisEvent redisEvent) {

        boolean flag = false;

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm", Locale.ENGLISH);
        Date nameDate = new Date();
        String name = "Redis_" + sdf.format(nameDate) + "_" + node.getMaster().getAddress();


        //total_connections_received
        if (0 == redisBaselineService.findByName(name).size()) {
            return flag;
        }

        //total_connections
        if (fluctTooMuch((double) node.getMaster().getInfo().getTotal_connections(), (double) redisBaselineService.findByName(name).get(0).getTotal_connections())) {

            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + TOTAL_CONNECTIONS;
            putToChannel(alarmConfig, TOTAL_CONNECTIONS, item, redisTemplate, node, detail, redisEvent, null);

        }

        //connected_clients
        if (fluctTooMuch((double) node.getMaster().getInfo().getConnected_clients(), (double) redisBaselineService.findByName(name).get(0).getConnected_clients())) {

            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + CONNECTED_CLIENTS;
            putToChannel(alarmConfig, CONNECTED_CLIENTS, item, redisTemplate, node, detail, redisEvent, null);

        }


        //input_kbps
        if (fluctTooMuch((double) node.getMaster().getInfo().getInput_kbps(), (double) redisBaselineService.findByName(name).get(0).getInput_kbps())) {
            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + INPUT_KBPS;
            putToChannel(alarmConfig, INPUT_KBPS, item, redisTemplate, node, detail, redisEvent, null);

        }

        //output_kbps
        if (fluctTooMuch((double) node.getMaster().getInfo().getOutput_kbps(), (double) redisBaselineService.findByName(name).get(0).getOutput_kbps())) {

            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + OUTPUT_KBPS;
            putToChannel(alarmConfig, OUTPUT_KBPS, item, redisTemplate, node, detail, redisEvent, null);


        }

        //used_cpu_sys
        if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_sys(), (double) redisBaselineService.findByName(name).get(0).getUsed_cpu_sys())) {
            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_SYS;
            putToChannel(alarmConfig, USED_CPU_SYS, item, redisTemplate, node, detail, redisEvent, null);

        }


        //used_cpu_user
        if (fluctTooMuch((double) node.getMaster().getInfo().getUsed_cpu_user(), (double) redisBaselineService.findByName(name).get(0).getUsed_cpu_user())) {

            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + USED_CPU_USER;
            putToChannel(alarmConfig, USED_CPU_USER, item, redisTemplate, node, detail, redisEvent, null);

        }

        return flag;
    }

    private boolean isQpsFlucAlarm(RedisClusterData item, RedisNode node, AlarmConfig alarmConfig, RedisTemplate redisTemplate, RedisEvent redisEvent) {
        boolean flag = false;

        //QPS
        if (null == node.getMaster() || null == node.getMaster().getInfo()) {
            return flag;
        }

        boolean qpsSwitch = redisTemplate.isQpsSwitch();
        int qpsFluc = redisTemplate.getQpsFluc();
        int qpsBase = redisTemplate.getQpsBase();
        int qpsInterval = redisTemplate.getQpsInterval();

        //短时间内波动分析
        //1.开关 2.是否高于flucBase 3.上升率 4.历史数据分析

        Server server = serverService.findByAddress(node.getMaster().getAddress());
        if(null == server){
            return flag;
        }
        int id =  server.getId();
        RedisStats redisStat = redisStatsFlucService.getRedisStatsByTime(qpsInterval,id);
        if (null != redisStat) {

//            long minQps = redisStat.getQps();
            long minQps = Long.parseLong(getMinVal(QPS, node, qpsInterval,node.getMaster().getInfo().getQps()).toString());
            if (qpsSwitch && (0 != minQps) && (node.getMaster().getInfo().getQps() < qpsBase)) {

                boolean alarmFlag = true;

                if ((node.getMaster().getInfo().getQps() - minQps) > qpsFluc) {

                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm", Locale.ENGLISH);
                    Date nameDate = new Date();

                    for (int i = -1; i < 1; i++) {
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.setTime(nameDate);
                        gc.add(12, i);
                        String name = "Redis_" + sdf.format(gc.getTime()) + "_" + node.getMaster().getAddress();

                        RedisBaseline redisBaseline = baselineCacheService.getRedisBaselineByName(name);
                        if (null == redisBaseline) {
                            continue;
                        }

                        if ((node.getMaster().getInfo().getQps() - redisBaseline.getQps()) < redisBaseline.getQps() * 0.1) {
                            alarmFlag = false;
                            break;
                        }
                    }

                    if (alarmFlag) {
                        String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + QPS_INCREASE_TOO_MUCH + ";QPS在" + qpsInterval + "分钟内从" + minQps + "增长到" + node.getMaster().getInfo().getQps();

                        flag = true;
                        String val = QPS_INCREASE_TOO_MUCH + ",QPS在" + qpsInterval + "分钟内从" + minQps + "增长到" + node.getMaster().getInfo().getQps();

                        putToChannel(alarmConfig, QPS_INCREASE_TOO_MUCH, item, redisTemplate, node, detail, redisEvent, val);

                    }
                }
            }
        }

        return flag;
    }

    private boolean isQpsAlarm(RedisClusterData item, RedisNode node, AlarmConfig alarmConfig, RedisTemplate redisTemplate, RedisEvent redisEvent) {

        boolean flag = false;

        //QPS
        if (null == node.getMaster() || null == node.getMaster().getInfo()) {
            return flag;
        }

        if (node.getMaster().getInfo().getQps() > redisTemplate.getQpsThreshold()) {
            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + QPS_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getQps();
            putToChannel(alarmConfig, QPS_TOO_HIGH, item, redisTemplate, node, detail, redisEvent, node.getMaster().getInfo().getQps());

        }
        return flag;
    }

    private boolean isMemFlucAlarm(RedisClusterData item, RedisNode node, AlarmConfig alarmConfig, RedisTemplate redisTemplate, RedisEvent redisEvent) {

        boolean flag = false;

        boolean memSwitch = redisTemplate.isMemSwitch();
        int memFluc = redisTemplate.getMemFluc();
        int memBase = redisTemplate.getMemBase();
        int memInterval = redisTemplate.getMemInterval();

        //短时间内波动分析
        //1.开关 2.是否高于flucBase 3.上升率 4.历史数据分析

//        float minMemUsage = redisStatsFlucService.getRedisMemUsageByTime(memInterval, node.getMaster().getAddress());
        float minMemUsage = Float.parseFloat(getMinVal(MEMUSAGE, node, memInterval,node.getMaster().getInfo().getUsed()).toString());

        if (memSwitch && (0 != minMemUsage) && (node.getMaster().getInfo().getUsed() * 100 < memBase)) {

            boolean alarmFlag = true;

            if ((node.getMaster().getInfo().getUsed() - minMemUsage) * 100 > memFluc) {

                SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm", Locale.ENGLISH);
                Date nameDate = new Date();

                for (int i = -1; i < 1; i++) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(nameDate);
                    gc.add(12, i);
                    String name = "Redis_" + sdf.format(gc.getTime()) + "_" + node.getMaster().getAddress();

                    RedisBaseline redisBaseline = baselineCacheService.getRedisBaselineByName(name);
                    if (null == redisBaseline) {
                        continue;
                    }

                    if ((node.getMaster().getInfo().getUsed() - redisBaseline.getMem()) < redisBaseline.getMem() * 0.1) {
                        alarmFlag = false;
                        break;
                    }
                }

                if (alarmFlag) {
                    flag = true;
                    String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + MEMUSAGE_INCREASE_TOO_MUCH + ";使用率在" + memInterval + "分钟内从" + minMemUsage + "增长到" + node.getMaster().getInfo().getUsed();

                    String val = MEMUSAGE_INCREASE_TOO_MUCH + ",使用率在" + memInterval + "分钟内从" + minMemUsage + "增长到" + node.getMaster().getInfo().getUsed();

                    putToChannel(alarmConfig, MEMUSAGE_INCREASE_TOO_MUCH, item, redisTemplate, node, detail, redisEvent, val);

                }
            }

        }

        return flag;
    }

    private boolean isMemAlarm(RedisClusterData item, RedisNode node, AlarmConfig alarmConfig, RedisTemplate redisTemplate, RedisEvent redisEvent) {
        boolean flag = false;

        if (node.getMaster().getInfo().getUsed() > redisTemplate.getMemThreshold()) {
            flag = true;
            String detail = item.getClusterName() + ":" + node.getMaster().getAddress() + "," + MEMUSAGE_TOO_HIGH + ";使用率为" + node.getMaster().getInfo().getUsed();
            putToChannel(alarmConfig, MEMUSAGE_TOO_HIGH, item, redisTemplate, node, detail, redisEvent, node.getMaster().getInfo().getUsedMemory());
        }

        return flag;
    }


    private Object getMinVal(int type, RedisNode node, int interval,Object curVal) {
        String minName = "Redis_" + type + "_" + node.getMaster().getAddress();
        MinVal minVal = minValCacheService.getMinValByName(minName);
        if (null == minVal) {
            switch (type) {
                case MEMUSAGE:
                    float flucUsage = redisStatsFlucService.getRedisMemUsageByTime(interval, node.getMaster().getAddress());
                    minValCacheService.updateMinVal(minName, new MinVal(ALARMTYPE, type, new Date(), flucUsage));
                    break;
                case QPS:
                    Server server = serverService.findByAddress(node.getMaster().getAddress());
                    if(null == server){
                        break;
                    }
                    int id =  server.getId();
                    RedisStats redisStat = redisStatsFlucService.getRedisStatsByTime(interval, id);
                    long flucQps = redisStat.getQps();
                    minValCacheService.updateMinVal(minName, new MinVal(ALARMTYPE, type, new Date(), flucQps));
                    break;
            }
        } else {
            MinVal curMinVal = new MinVal(ALARMTYPE, type, new Date(), curVal);
            boolean update = minValCacheService.checkForUpdate(minName, curMinVal);
            if (update) {
                minValCacheService.updateMinVal(minName, curMinVal);
            }
            boolean isExpire = minValCacheService.isExpire(minName, interval);
            if (isExpire) {
                Object tmpMinVal = null;
                for (int i = 0; i < interval; i++) {
                    switch (type) {
                        case MEMUSAGE:
                            float flucUsage = redisStatsFlucService.getRedisMemUsageByTime(i, node.getMaster().getAddress());
                            if (null == tmpMinVal) {
                                tmpMinVal = flucUsage;
                            } else {
                                if (Float.parseFloat(tmpMinVal.toString()) > flucUsage) {
                                    tmpMinVal = flucUsage;
                                }
                            }
                            break;
                        case QPS:
                            Server server = serverService.findByAddress(node.getMaster().getAddress());
                            if(null == server){
                                break;
                            }
                            int id =  server.getId();
                            RedisStats redisStat = redisStatsFlucService.getRedisStatsByTime(i, id);
                            long flucQps = redisStat.getQps();
                            if (null == tmpMinVal) {
                                tmpMinVal = flucQps;
                            } else {
                                if (Long.parseLong(tmpMinVal.toString()) > flucQps) {
                                    tmpMinVal = flucQps;
                                }
                            }
                            break;
                    }
                }

                MinVal newMinVal = new MinVal(ALARMTYPE,type,new Date(),tmpMinVal);
                minValCacheService.updateMinVal(minName, newMinVal);
            }
        }
        return minValCacheService.getMinValByName(minName).getVal();
    }

    private boolean masterSlaveConsistency(RedisClusterData item, RedisEvent redisEvent) {

        boolean flag = true;
        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

        if ((null == alarmConfig) && (null != item.getClusterName())) {
            alarmConfig = new AlarmConfig("Redis", item.getClusterName());
            alarmConfigService.insert(alarmConfig);
        }

        RedisTemplate redisTemplate = redisAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        //主从数量不一致告警
        if (item.getMasterNum() != item.getSlaveNum()) {
            AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
            flag = true;
            alarmDetail.setAlarmTitle(MASTER_SLAVE_DIFF)
                    .setAlarmDetail(item.getClusterName() + ":" + MASTER_SLAVE_DIFF)
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

        return flag;
    }

    private boolean isDownAlarm(RedisClusterData item, List<RedisClusterData> redisClusterDatas, RedisEvent redisEvent) {
        boolean flag = false;
        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getClusterName());

        if ((null == alarmConfig) && (null != item.getClusterName())) {
            alarmConfig = new AlarmConfig("Redis", item.getClusterName());
            alarmConfigService.insert(alarmConfig);
        }

        RedisTemplate redisTemplate = redisAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        //cluster down
        if (redisTemplate.isDown()) {
            for (RedisServer redisServer : item.getFailedServers()) {
                AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);
                flag = true;
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

        return flag;
    }

    private void putToChannel(AlarmConfig alarmConfig, String type, RedisClusterData item, RedisTemplate redisTemplate, RedisNode node, String detail, RedisEvent redisEvent, Object o) {

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
                .setValue(String.valueOf(o))
                .setCreateTime(new Date());

        alarmRecordDao.insert(alarmRecord);

        redisEvent.put(alarmDetail);

    }


    private boolean fluctTooMuch(double cur, double base) {
        boolean result = false;

        if (0 == base) {
            return result;
        }

        if (Math.abs((cur - base)) / base > 0.5) {
            result = true;
        }

        return result;
    }

}
