package com.dianping.cache.alarm.memcache;

import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.entity.AlarmRecord;
import com.dianping.cache.alarm.entity.MemcacheTemplate;
import com.dianping.cache.alarm.event.EventFactory;
import com.dianping.cache.alarm.event.EventType;
import com.dianping.cache.alarm.report.EventReporter;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.monitor.MemcachedClientFactory;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerService;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by lvshiyun on 15/11/21.
 */
@Service
public class MemcacheAlarmer extends AbstractMemcacheAlarmer {
    //从数据库拉出当前最近的一次数据，然后检查相应的配置文件，是否符合报警条件，符合则生成报警事件放入event队列

    private static final String CLUSTER_DOWN = "集群实例无法连接";
    private static final String MEMUSAGE_TOO_HIGH = "内存使用率过高";
    private static final String QPS_TOO_HIGH = "QPS过高";
    private static final String CONN_TOO_HIGH = "连接数过高";

    private static final String ALARMTYPE = "Memcache";

    @Autowired
    private ServerService serverService;

    @Autowired
    private MemcacheStatsService memcacheStatsService;

    @Autowired
    private CacheConfigurationService cacheConfigurationService;

    @Autowired
    protected EventFactory eventFactory;

    @Autowired
    protected EventReporter eventReporter;

    @Autowired
    AlarmRecordDao alarmRecordDao;

    @Autowired
    AlarmConfigService alarmConfigService;

    @Autowired
    MemcacheAlarmTemplateService memcacheAlarmTemplateService;

    @Override
    public void doAlarm() throws InterruptedException, MemcachedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException, MemcachedException, IOException, TimeoutException {

        MemcacheEvent memcacheEvent = eventFactory.createMemcacheEvent();

        MemcacheData memcacheData = new MemcacheData();

        memcacheData.setCacheConfigurationService(cacheConfigurationService);
        memcacheData.setMemcacheStatsService(memcacheStatsService);
        memcacheData.setServerService(serverService);

        Map<String, Map<String, Object>> currentServerStats = memcacheData.getCurrentServerStatsData();

        List<CacheConfiguration> configList = cacheConfigurationService.findAll();


        boolean isReport = false;

        List<AlarmType> types = new ArrayList<AlarmType>();
        for (CacheConfiguration item : configList) {
            //遍历所有的集群  对于集群名称为memcached的进行检查并放入告警队列
            if (item.getCacheKey().contains("memcached")
                    && !"memcached-leo".equals(item.getCacheKey())) {
                AlarmDetail downAlarm = isDownAlarm(item, currentServerStats, memcacheEvent);
                if (null != downAlarm) {
                    isReport = true;
                }

                AlarmDetail memAlarm = isMemAlarm(item, currentServerStats, memcacheEvent);
                if (null != memAlarm) {
                    isReport = true;
                }

                AlarmDetail qpsAlarm = isQpsAlarm(item, currentServerStats, memcacheEvent);
                if (null != qpsAlarm) {
                    isReport = true;
                }

                AlarmConfig connAlarm = isConnAlarm(item, currentServerStats, memcacheEvent);
                if (null != connAlarm) {
                    isReport = true;
                }
            }
        }

        if (isReport) {
            memcacheEvent.setEventType(EventType.MEMCACHE).setCreateTime(new Date());

            eventReporter.report(memcacheEvent);

        }
    }

    AlarmDetail isDownAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException, IOException, MemcachedException, TimeoutException {

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        List<String> serverList = item.getServerList();

        for (String server : serverList) {
            String[] splitText = server.split(":");
            String ip = splitText[0];
            int port = Integer.parseInt(splitText[1]);

            MemcachedClient mc = MemcachedClientFactory.getMemcachedClient(server);
            Map<String, String> stats = null;
            try {
                stats = mc.stats(new InetSocketAddress(ip, port), 1000);
            } catch (Exception e) {
                AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

                MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmDetail.getAlarmTemplate());

                alarmDetail.setClusterName(item.getCacheKey());
                alarmDetail.setAlarmTitle(CLUSTER_DOWN)
                        .setAlarmDetail(item.getCacheKey() + ":" + CLUSTER_DOWN + ";机器信息为" + server)
                        .setMailMode(memcacheTemplate.isMailMode())
                        .setSmsMode(memcacheTemplate.isSmsMode())
                        .setWeixinMode(memcacheTemplate.isWeixinMode())
                        .setCreateTime(new Date());


                AlarmRecord alarmRecord = new AlarmRecord();
                alarmRecord.setAlarmType(AlarmType.MEMCACHE_CLUSTER_DOWN.getNumber())
                        .setAlarmTitle(CLUSTER_DOWN)
                        .setAlarmDetail(item.getCacheKey() + ":" + CLUSTER_DOWN + ";机器信息为" + server)
                        .setCreateTime(new Date());

                alarmRecordDao.insert(alarmRecord);

                memcacheEvent.put(alarmDetail);
                return alarmDetail;
            }
        }

        return null;
    }


    AlarmDetail isMemAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException {

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }
        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        List<String> serverList = item.getServerList();

        long mem = 0;
        long memused = 0;
        float usage = 0;

        String ips = "";

        for (String server : serverList) {
            ips += server + ",";
            if (0 != currentServerStats.size()) {
                Long tmp = (Long) currentServerStats.get(server).get("max_memory");
                if (null != tmp) {
                    mem += tmp;
                }

                tmp = (Long) currentServerStats.get(server).get("used_memory");
                if (null != tmp) {
                    memused += tmp;
                }
            }
        }
        if (0 != mem) {
            usage = (float) memused / mem;
        }

        if (usage * 100 > memcacheTemplate.getMemThreshold()) {
            AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

            alarmDetail.setAlarmTitle(MEMUSAGE_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + MEMUSAGE_TOO_HIGH + ";IP为" + ips + ";使用率为" + usage)
                    .setMailMode(memcacheTemplate.isMailMode())
                    .setSmsMode(memcacheTemplate.isSmsMode())
                    .setWeixinMode(memcacheTemplate.isWeixinMode())
                    .setCreateTime(new Date());

            AlarmRecord alarmRecord = new AlarmRecord();
            alarmRecord.setAlarmType(AlarmType.MEMCACHE_MEMUSAGE_TOO_HIGH.getNumber())
                    .setAlarmTitle(MEMUSAGE_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + MEMUSAGE_TOO_HIGH + ";IP为" + ips + ";使用率为" + usage)
                    .setCreateTime(new Date());

            alarmRecordDao.insert(alarmRecord);

            memcacheEvent.put(alarmDetail);
            return alarmDetail;
        }

        return null;
    }

    AlarmDetail isQpsAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException {

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        List<String> serverList = item.getServerList();

        long qps = 0;

        String ips = "";

        for (String server : serverList) {
            ips += server + ",";
            if (0 != currentServerStats.size()) {
                Long tmp = (Long) currentServerStats.get(server).get("QPS");
                if (null != tmp) {
                    qps += tmp;
                }
            }
        }

        if (qps > memcacheTemplate.getQpsThreshold()) {
            AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

            alarmDetail.setAlarmTitle(QPS_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + QPS_TOO_HIGH + ";IP为" + ips + ";QPS为" + qps)
                    .setMailMode(memcacheTemplate.isMailMode())
                    .setSmsMode(memcacheTemplate.isSmsMode())
                    .setWeixinMode(memcacheTemplate.isWeixinMode())
                    .setCreateTime(new Date());

            AlarmRecord alarmRecord = new AlarmRecord();
            alarmRecord.setAlarmType(AlarmType.MEMCACHE_QPS_TOO_HIGH.getNumber())
                    .setAlarmTitle(QPS_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + QPS_TOO_HIGH + ";IP为" + ips + ";QPS为" + qps)
                    .setCreateTime(new Date());

            alarmRecordDao.insert(alarmRecord);


            memcacheEvent.put(alarmDetail);
            return alarmDetail;
        }

        return null;

    }

    AlarmDetail isConnAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException {

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        List<String> serverList = item.getServerList();

        int conn = 0;

        String ips = "";

        for (String server : serverList) {

            ips += server + ",";

            if (0 != currentServerStats.size()) {
                Integer tmp = (Integer) currentServerStats.get(server).get("curr_conn");
                if (null != tmp) {
                    conn += tmp;
                }
            }
        }

        if (conn > memcacheTemplate.getConnThreshold()) {
            AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

            alarmDetail.setAlarmTitle(CONN_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + CONN_TOO_HIGH + ";IP为" + ips + ";连接数为" + conn)
                    .setMailMode(memcacheTemplate.isMailMode())
                    .setSmsMode(memcacheTemplate.isSmsMode())
                    .setWeixinMode(memcacheTemplate.isWeixinMode())
                    .setCreateTime(new Date());

            AlarmRecord alarmRecord = new AlarmRecord();
            alarmRecord.setAlarmType(AlarmType.MEMCACHE_CONN_TOO_HIGH.getNumber())
                    .setAlarmTitle(CONN_TOO_HIGH)
                    .setAlarmDetail(item.getCacheKey() + ":" + CONN_TOO_HIGH + ";IP为" + ips + ";连接数为" + conn)
                    .setCreateTime(new Date());

            alarmRecordDao.insert(alarmRecord);

            memcacheEvent.put(alarmDetail);
            return alarmDetail;
        }

        return null;
    }

    public CacheConfigurationService getCacheConfigurationService() {
        return cacheConfigurationService;
    }

    public void setCacheConfigurationService(CacheConfigurationService cacheConfigurationService) {
        this.cacheConfigurationService = cacheConfigurationService;
    }
}
