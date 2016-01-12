package com.dianping.cache.alarm.memcache;

import com.dianping.cache.alarm.AlarmType;
import com.dianping.cache.alarm.alarmconfig.AlarmConfigService;
import com.dianping.cache.alarm.alarmtemplate.MemcacheAlarmTemplateService;
import com.dianping.cache.alarm.dao.AlarmRecordDao;
import com.dianping.cache.alarm.dataanalyse.service.MemcacheBaselineService;
import com.dianping.cache.alarm.entity.AlarmConfig;
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
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private static final String SET_FLUC_TOO_MUCH = "set波动过大";
    private static final String GET_FLUC_TOO_MUCH = "get波动过大";
    private static final String WRITE_BYTES_FLUC_TOO_MUCH = "write_bytes波动过大";
    private static final String READ_BYTES_FLUC_TOO_MUCH = "read_bytes波动过大";

    private static final String EVICT_FLUC_TOO_MUCH = "evict波动过大";

    private static final String HITRATE_FLUC_TOO_MUCH = "hitrate波动过大";

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


    @Autowired
    MemcacheBaselineService memcacheBaselineService;

    @Override
    public void doAlarm() throws InterruptedException, IOException, TimeoutException {
        doCheck();
    }

    private void doCheck() throws InterruptedException, IOException, TimeoutException {

        MemcacheEvent memcacheEvent = eventFactory.createMemcacheEvent();

        MemcacheData memcacheData = new MemcacheData();

        memcacheData.setCacheConfigurationService(cacheConfigurationService);
        memcacheData.setMemcacheStatsService(memcacheStatsService);memcacheData.setServerService(serverService);


        Map<String, Map<String, Object>> currentServerStats = memcacheData.getCurrentServerStatsData();

        List<CacheConfiguration> configList = cacheConfigurationService.findAll();


        boolean isReport = false;

        List<AlarmType> types = new ArrayList<AlarmType>();
        for (CacheConfiguration item : configList) {
            //遍历所有的集群  对于集群名称为memcached的进行检查并放入告警队列
            if (item.getCacheKey().contains("memcached")
                    && !"memcached-leo".equals(item.getCacheKey())) {
                boolean downAlarm = isDownAlarm(item, currentServerStats, memcacheEvent);
                if (downAlarm) {
                    isReport = true;
                }

                boolean memAlarm = isMemAlarm(item, currentServerStats, memcacheEvent);
                if (memAlarm) {
                    isReport = true;
                }

                boolean qpsAlarm = isQpsAlarm(item, currentServerStats, memcacheEvent);
                if (qpsAlarm) {
                    isReport = true;
                }

                boolean connAlarm = isConnAlarm(item, currentServerStats, memcacheEvent);
                if (connAlarm) {
                    isReport = true;
                }

                boolean history = isHistoryAlarm(item, currentServerStats, memcacheEvent);
                if(history){
                    isReport = true;
                }

            }
        }

        if (isReport) {
            memcacheEvent.setEventType(EventType.MEMCACHE).setCreateTime(new Date());

            eventReporter.report(memcacheEvent);

        }
    }

    boolean isDownAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException, IOException, TimeoutException {

        boolean flag = false;

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

            MemcachedClient mc = MemcachedClientFactory.getInstance().getClient(server);
            Map<String, String> stats = null;
            try {
                stats = mc.getStats().get(new InetSocketAddress(ip, port));
            } catch (Exception e) {

                String detail = item.getCacheKey() + ":" + CLUSTER_DOWN + ";机器信息为" + server;

                flag = putToChannel(alarmConfig,CLUSTER_DOWN,memcacheEvent,item,ip, detail,null);

            }
        }

        return flag;
    }


    boolean isMemAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException {

        boolean flag = false;

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }
        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        if (null == memcacheTemplate) {
            logger.info(item.getCacheKey() + "not config template");
            memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName("Default");
        }

        List<String> serverList = item.getServerList();

        long mem = 0;
        long memused = 0;
        float usage = 0;

        String ip = "";

        for (String server : serverList) {
            ip = server;
            if (0 != currentServerStats.size()) {
                if (null != currentServerStats.get(server)) {
                    Long tmp = (Long) currentServerStats.get(server).get("max_memory");
                    if (null != tmp) {
                        mem = tmp;
                    }

                    tmp = (Long) currentServerStats.get(server).get("used_memory");
                    if (null != tmp) {
                        memused = tmp;
                    }
                } else {
                    continue;
                }
            }


            if (0 != mem) {
                usage = (float) memused / mem;
            }

            if (usage * 100 > memcacheTemplate.getMemThreshold()) {

                String detail = item.getCacheKey() + ":" + MEMUSAGE_TOO_HIGH + ",IP为" + ip + ";使用率为" + usage;

                flag = putToChannel(alarmConfig,MEMUSAGE_TOO_HIGH,memcacheEvent,item,ip, detail,usage * 100);


            }
        }

        return flag;
    }

    boolean isQpsAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException {

        boolean flag = false;

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        if (null == memcacheTemplate) {
            logger.info(item.getCacheKey() + "not config template");
            memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName("Default");
        }

        List<String> serverList = item.getServerList();

        long qps = 0;

        String ip = "";

        for (String server : serverList) {
            ip = server;
            if (0 != currentServerStats.size()) {
                if (null != currentServerStats.get(server)) {
                    Long tmp = (Long) currentServerStats.get(server).get("QPS");
                    if (null != tmp) {
                        qps = tmp;
                    }
                } else {
                    continue;
                }
            }


            if (qps > memcacheTemplate.getQpsThreshold()) {
                String detail = item.getCacheKey() + ":" + QPS_TOO_HIGH + ",IP为" + ip + ";QPS为" + qps;

                flag = putToChannel(alarmConfig,QPS_TOO_HIGH,memcacheEvent,item,ip, detail,qps);

            }
        }

        return flag;

    }

    boolean isConnAlarm(CacheConfiguration
                                item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws
            InterruptedException {
        boolean flag = false;

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        if (null == memcacheTemplate) {
            logger.info(item.getCacheKey() + "not config template");
            memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName("Default");
        }

        List<String> serverList = item.getServerList();

        int conn = 0;

        String ip = "";

        for (String server : serverList) {

            ip = server;

            if (0 != currentServerStats.size()) {
                if (null != currentServerStats.get(server)) {
                    Integer tmp = (Integer) currentServerStats.get(server).get("curr_conn");
                    if (null != tmp) {
                        conn = tmp;
                    }
                } else {
                    continue;
                }
            }


            if (conn > memcacheTemplate.getConnThreshold()) {

                String detail = item.getCacheKey() + ":" + CONN_TOO_HIGH + ",IP为" + ip + ";连接数为" + conn;

                flag = putToChannel(alarmConfig,QPS_TOO_HIGH,memcacheEvent,item,ip, detail,conn);

            }
        }

        return flag;
    }

    boolean isHistoryAlarm(CacheConfiguration item, Map<String, Map<String, Object>> currentServerStats, MemcacheEvent memcacheEvent) throws InterruptedException, IOException, TimeoutException {

        boolean flag = false;

        AlarmConfig alarmConfig = alarmConfigService.findByClusterTypeAndName(ALARMTYPE, item.getCacheKey());

        if (null == alarmConfig) {
            alarmConfig = new AlarmConfig("Memcache", item.getCacheKey());
            alarmConfigService.insert(alarmConfig);
        }

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmConfig.getAlarmTemplate());

        if (null == memcacheTemplate) {
            logger.info(item.getCacheKey() + "not config template");
            memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName("Default");
        }

        if(!memcacheTemplate.isCheckHistory()){
            return false;
        }

        List<String> serverList = item.getServerList();

        long set = 0;
        long get = 0;
        long write_bytes = 0;
        long read_bytes = 0;

        long evict = 0;
        float hitrate = 0;

        String ip = "";

        for (String server : serverList) {

            ip = server;

            if (0 != currentServerStats.size()) {
                if (null != currentServerStats.get(server)) {
                    Long settmp = (Long)currentServerStats.get(server).get("set");
                    Long gettmp = (Long)currentServerStats.get(server).get("get");
                    Long write_bytestmp = (Long)currentServerStats.get(server).get("write_bytes");
                    Long read_bytestmp = (Long)currentServerStats.get(server).get("read_bytes");

                    Long evicttmp = (Long) currentServerStats.get(server).get("evict");
                    Float hitratetmp = (Float) currentServerStats.get(server).get("hitrate");

                    if ((null != evicttmp) && (null != hitratetmp)) {
                        set = settmp;
                        get = gettmp;
                        write_bytes = write_bytestmp;
                        read_bytes = read_bytestmp;

                        evict = evicttmp;
                        hitrate = hitratetmp;
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm", Locale.ENGLISH);
            Date nameDate = new Date();
            String name = "Memcache_" + sdf.format(nameDate)+"_"+server;

            if(null == memcacheBaselineService.findByName(name)){
                return false;
            }

            if(0 ==memcacheBaselineService.findByName(name).size()){
                return false;
            }

            double base_set = (double) memcacheBaselineService.findByName(name).get(0).getCmd_set();

            if (fluctTooMuch((double) set, base_set)) {

                String detail = item.getCacheKey() + ":" + SET_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,SET_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

            double base_get = (double) memcacheBaselineService.findByName(name).get(0).getGet_hits();
            if (fluctTooMuch((double) get, base_get)) {

                String detail = item.getCacheKey() + ":" + GET_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,GET_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

            double base_write_bytes=(double) memcacheBaselineService.findByName(name).get(0).getBytes_written();

            if (fluctTooMuch((double) write_bytes,base_write_bytes )) {

                String detail = item.getCacheKey() + ":" + WRITE_BYTES_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,WRITE_BYTES_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

            double base_read_bytes = (double) memcacheBaselineService.findByName(name).get(0).getBytes_read();

            if (fluctTooMuch((double) read_bytes, base_read_bytes)) {

                String detail = item.getCacheKey() + ":" + READ_BYTES_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,READ_BYTES_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

            double base_evict = (double) memcacheBaselineService.findByName(name).get(0).getEvictions();
            if (fluctTooMuch((double) evict, base_evict)) {

                String detail = item.getCacheKey() + ":" + EVICT_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,EVICT_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

            double base_hitrate = (double) memcacheBaselineService.findByName(name).get(0).getGet_hits() / (memcacheBaselineService.findByName(name).get(0).getGet_hits() + memcacheBaselineService.findByName(name).get(0).getDelete_hits());
            if (fluctTooMuch((double) hitrate, base_hitrate)) {

                String detail = item.getCacheKey() + ":" + HITRATE_FLUC_TOO_MUCH + ",IP为" + ip;

                flag = putToChannel(alarmConfig,HITRATE_FLUC_TOO_MUCH,memcacheEvent,item,ip, detail,null);

            }

        }

        return flag;
    }

    private boolean putToChannel(AlarmConfig alarmConfig, String type, MemcacheEvent memcacheEvent, CacheConfiguration item, String ip, String detail, Object o) {
        AlarmDetail alarmDetail = new AlarmDetail(alarmConfig);

        MemcacheTemplate memcacheTemplate = memcacheAlarmTemplateService.findAlarmTemplateByTemplateName(alarmDetail.getAlarmTemplate());

        if(!memcacheTemplate.isDown()){
            return false;
        }

        alarmDetail.setClusterName(item.getCacheKey());
        alarmDetail.setAlarmTitle(item.getCacheKey()+type)
                .setAlarmDetail(detail)
                .setMailMode(memcacheTemplate.isMailMode())
                .setSmsMode(memcacheTemplate.isSmsMode())
                .setWeixinMode(memcacheTemplate.isWeixinMode())
                .setCreateTime(new Date());


        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setAlarmTitle(CLUSTER_DOWN)
                .setClusterName(item.getCacheKey())
                .setIp(ip)
                .setValue((Float)o)
                .setCreateTime(new Date());

        alarmRecordDao.insert(alarmRecord);

        memcacheEvent.put(alarmDetail);


        return true;
    }


    private boolean fluctTooMuch(double v1, double v2) {
        boolean result = false;

        if(0 == v2){
            return result;
        }

        if (Math.abs((v1 - v2)) / v2 > 0.01) {
            result = true;
        }

        return result;
    }


    public CacheConfigurationService getCacheConfigurationService() {
        return cacheConfigurationService;
    }

    public void setCacheConfigurationService(CacheConfigurationService cacheConfigurationService) {
        this.cacheConfigurationService = cacheConfigurationService;
    }
}
