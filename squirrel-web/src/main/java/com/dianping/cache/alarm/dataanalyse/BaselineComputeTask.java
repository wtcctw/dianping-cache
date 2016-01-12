package com.dianping.cache.alarm.dataanalyse;

import com.dianping.cache.alarm.dataanalyse.mapper.MemcacheBaselineMapper;
import com.dianping.cache.alarm.dataanalyse.mapper.RedisBaselineMapper;
import com.dianping.cache.alarm.dataanalyse.service.BaselineComputeTaskService;
import com.dianping.cache.alarm.dataanalyse.service.MemcacheBaselineService;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;
import com.dianping.cache.entity.MemcacheStats;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.RedisStatsService;
import com.dianping.cache.service.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineComputeTask")
@Scope("prototype")
public class BaselineComputeTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    MemcacheStatsService memcacheStatsService;

    @Autowired
    RedisStatsService redisStatsService;

    @Autowired
    MemcacheBaselineService memcacheBaselineService;


    @Autowired
    RedisBaselineService redisBaselineService;

    @Autowired
    BaselineComputeTaskService baselineComputeTaskService;

//    @Autowired
//    BaselineCacheService baselineCacheService;

    @Autowired
    ServerService serverService;


    public void run() {

        try {
            Date now = new Date();
            com.dianping.cache.alarm.entity.BaselineComputeTask baselinecomputeTask = new com.dianping.cache.alarm.entity.BaselineComputeTask();
            baselinecomputeTask.setCreateTime(now);

            baselineComputeTaskService.insert(baselinecomputeTask);

            int taskId = baselineComputeTaskService.getRecentTaskId().get(0).getId();


            memcacheBaselineCompute(taskId);

            redisBaselineCompute(taskId);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private void memcacheBaselineCompute(int taskId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        List<String> startTime = getStartTime();
        String endTime = df.format(new Date());

        Map<String, MemcacheStats> memcacheStatses = getMemcacheState(startTime, endTime);

        memcacheBaslineStoreToDb(taskId, memcacheStatses);


    }

    private void memcacheBaslineStoreToDb(int taskId, Map<String, MemcacheStats> memcacheStatses) {

        Map<String, MemcacheBaseline> memcacheBaselineMap = new HashMap<String, MemcacheBaseline>();

        for (Map.Entry<String, MemcacheStats> entry : memcacheStatses.entrySet()) {
            MemcacheBaseline memcacheBaseline = MemcacheBaselineMapper.convertToMemcacheBaseline(entry.getValue());
            memcacheBaseline.setBaseline_name(entry.getKey());
            memcacheBaseline.setTaskId(taskId);

            memcacheBaselineService.insert(memcacheBaseline);
            memcacheBaselineMap.put(memcacheBaseline.getBaseline_name(), memcacheBaseline);
        }

//        baselineCacheService.putMemcacheBaselineMapToCache(memcacheBaselineMap);

    }

    private void redisBaselineCompute(int taskId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        List<String> startTime = getStartTime();
        String endTime = df.format(new Date());

        Map<String, RedisStats> redisStatsMap = getRedisState(startTime, endTime);

        redisBaslineStoreToDb(taskId, redisStatsMap);

    }

    private void redisBaslineStoreToDb(int taskId, Map<String, RedisStats> redisStatsMap) {
        Map<String, RedisBaseline> redisBaselineMap = new HashMap<String, RedisBaseline>();

        for (Map.Entry<String, RedisStats> entry : redisStatsMap.entrySet()) {
            RedisBaseline redisBaseline = RedisBaselineMapper.convertToRedisBaseline(entry.getValue());
            redisBaseline.setBaseline_name(entry.getKey());
            redisBaseline.setTaskId(taskId);
            redisBaselineService.insert(redisBaseline);
            redisBaselineMap.put(redisBaseline.getBaseline_name(), redisBaseline);
        }

//        baselineCacheService.putRedisBaselineMapToCache(redisBaselineMap);
    }


    private Map<String, MemcacheStats> getMemcacheState(List<String> startTimeList, String endDate) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);

        Map<String, MemcacheStats> memcacheStatsMap = new HashMap<String, MemcacheStats>();

        for (int i = 0; i < startTimeList.size(); i++) {
            String startTime = startTimeList.get(i);
            startTime = startTime.split(" ")[0];
            String endTime;
            if (i < startTimeList.size() - 1) {
                endTime = startTimeList.get(i + 1);
            } else {
                endTime = endDate;
            }
            endTime = endTime.split(" ")[0];

            startTime += " 00:00:00";
            endTime += " 00:00:00";


            long startLong = df.parse(startTime).getTime() / 1000;
            long tmp = startLong;
            long endLong = df.parse(endTime).getTime() / 1000;

            List<Server> serverList = serverService.findAllMemcachedServers();

            while (tmp + 60 < endLong) {//每分钟只采样一次
                long end = tmp + 120;

                for (Server server : serverList) {

                    String sql = "SELECT id,serverId, uptime, curr_time, total_conn, curr_conn, curr_items, cmd_set, get_hits, get_misses," +
                            " bytes_read, bytes_written, delete_hits, delete_misses, evictions,limit_maxbytes, bytes " +
                            "FROM memcache_stats " +
                            " WHERE curr_time > " + tmp + " AND curr_time <= " + end + " AND serverId =" + server.getId();

                    List<MemcacheStats> memcacheStatsestmp = memcacheStatsService.search(sql);
                    if (0 != memcacheStatsestmp.size()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm",Locale.ENGLISH);
                        Date nameDate = new Date(tmp * 1000);
                        String name = "Memcache_" + sdf.format(nameDate) + "_" + server.getAddress();

                        MemcacheStats memcacheStatsDelta = getMemcacheDelta(memcacheStatsestmp);

                        if (null == memcacheStatsDelta) {
                            continue;
                        }

                        if (null == memcacheStatsMap.get(name)) {
                            memcacheStatsMap.put(name, memcacheStatsDelta);
                        } else {
                            MemcacheStats memcacheStats = dealMemcacheStats(memcacheStatsDelta, memcacheStatsMap.get(name));

                            memcacheStatsMap.remove(name);
                            memcacheStatsMap.put(name, memcacheStats);
                        }
                    }

                }
                tmp += 60;
            }
            for (Server server : serverList) {

                String sql = "SELECT id,serverId, uptime, curr_time, total_conn, curr_conn, curr_items, cmd_set, get_hits, get_misses," +
                        " bytes_read, bytes_written, delete_hits, delete_misses, evictions,limit_maxbytes, bytes " +
                        "FROM memcache_stats " +
                        " WHERE curr_time > " + tmp + " AND curr_time <= " + endLong + " AND serverId =" + server.getId();
                List<MemcacheStats> memcacheStatsestmp = memcacheStatsService.search(sql);
                if (0 != memcacheStatsestmp.size()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm",Locale.ENGLISH);
                    Date nameDate = new Date(tmp * 1000);
                    String name = "Memcache_" + sdf.format(nameDate) + "_" + server.getAddress();
                    MemcacheStats memcacheStatsDelta = getMemcacheDelta(memcacheStatsestmp);
                    if (null == memcacheStatsDelta) {
                        continue;
                    }
                    if (null == memcacheStatsMap.get(name)) {
                        memcacheStatsMap.put(name, memcacheStatsDelta);
                    } else {
                        MemcacheStats memcacheStats = dealMemcacheStats(memcacheStatsDelta, memcacheStatsMap.get(name));

                        memcacheStatsMap.remove(name);
                        memcacheStatsMap.put(name, memcacheStats);
                    }
                }
            }
        }


        return memcacheStatsMap;

    }

    private MemcacheStats getMemcacheDelta(List<MemcacheStats> memcacheStatsestmp) {

        MemcacheStats memcacheStats = new MemcacheStats();
        if (memcacheStatsestmp.size() < 2) {
            return null;
        }

        memcacheStats.setId(memcacheStatsestmp.get(0).getId());
        memcacheStats.setServerId(memcacheStatsestmp.get(0).getServerId());
        memcacheStats.setCurr_time(memcacheStatsestmp.get(0).getCurr_time());

        memcacheStats.setBytes(Math.abs(memcacheStatsestmp.get(1).getBytes() - memcacheStatsestmp.get(0).getBytes()));

        memcacheStats.setBytes_read(Math.abs(memcacheStatsestmp.get(1).getBytes_read() - memcacheStatsestmp.get(0).getBytes_read()));

        memcacheStats.setBytes_written(Math.abs(memcacheStatsestmp.get(1).getBytes_written() - memcacheStatsestmp.get(0).getBytes_written()));

        memcacheStats.setCmd_set(Math.abs(memcacheStatsestmp.get(1).getCmd_set() - memcacheStatsestmp.get(0).getCmd_set()));

        memcacheStats.setCurr_conn(memcacheStatsestmp.get(0).getCurr_conn());

        memcacheStats.setDelete_hits(Math.abs(memcacheStatsestmp.get(1).getDelete_hits() - memcacheStatsestmp.get(0).getDelete_hits()));

        memcacheStats.setEvictions(Math.abs(memcacheStatsestmp.get(1).getEvictions() - memcacheStatsestmp.get(0).getEvictions()));

        memcacheStats.setDelete_misses(Math.abs(memcacheStatsestmp.get(1).getDelete_misses() - memcacheStatsestmp.get(0).getDelete_misses()));

        memcacheStats.setCurr_items( memcacheStatsestmp.get(0).getCurr_items());

        memcacheStats.setGet_hits(Math.abs(memcacheStatsestmp.get(1).getGet_hits() - memcacheStatsestmp.get(0).getGet_hits()));

        memcacheStats.setGet_misses(Math.abs(memcacheStatsestmp.get(1).getGet_misses() - memcacheStatsestmp.get(0).getGet_misses()));

        memcacheStats.setLimit_maxbytes(memcacheStatsestmp.get(0).getLimit_maxbytes());

        memcacheStats.setTotal_conn(memcacheStatsestmp.get(0).getTotal_conn());

        return memcacheStats;
    }

    private MemcacheStats dealMemcacheStats(MemcacheStats curr, MemcacheStats base) {
        MemcacheStats memcacheStats = new MemcacheStats();

        memcacheStats.setId(curr.getId());
        memcacheStats.setServerId(curr.getServerId());
        memcacheStats.setCurr_time(curr.getCurr_time());

        memcacheStats.setBytes((long) (compute(curr.getBytes(), base.getBytes())));

        memcacheStats.setBytes_read((long) (compute(curr.getBytes_read(), base.getBytes_read())));

        memcacheStats.setBytes_written((long) (compute(curr.getBytes_written(), base.getBytes_written())));

        memcacheStats.setCmd_set((long) (compute(curr.getCmd_set(), base.getCmd_set())));

        memcacheStats.setCurr_conn((int) (compute(curr.getCurr_conn(), base.getCurr_conn())));

        memcacheStats.setDelete_hits((long) (compute(curr.getDelete_hits(), base.getDelete_hits())));

        memcacheStats.setEvictions((long) (compute(curr.getEvictions(), base.getEvictions())));

        memcacheStats.setDelete_misses((long) (compute(curr.getDelete_misses(), base.getDelete_misses())));

        memcacheStats.setCurr_items((int) (compute(curr.getCurr_items(), base.getCurr_items())));

        memcacheStats.setGet_hits((long) (compute(curr.getGet_hits(), base.getGet_hits())));

        memcacheStats.setGet_misses((long) (compute(curr.getGet_misses(), base.getGet_misses())));

        memcacheStats.setLimit_maxbytes((long) (compute(curr.getLimit_maxbytes(), base.getLimit_maxbytes())));

        memcacheStats.setTotal_conn((int) (compute(curr.getTotal_conn(), base.getTotal_conn())));

        return memcacheStats;

    }

    private Map<String, RedisStats> getRedisState(List<String> startTimeList, String endDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);

        Map<String, RedisStats> redisStatsMap = new HashMap<String, RedisStats>();

        List<Server> serverList = serverService.findAllRedisServers();

        for (int i = 0; i < startTimeList.size(); i++) {
            String startTime = startTimeList.get(i);
            startTime = startTime.split(" ")[0];
            String endTime;
            if (i < startTimeList.size() - 1) {
                endTime = startTimeList.get(i + 1);
            } else {
                endTime = endDate;
            }
            endTime = endTime.split(" ")[0];

            startTime += " 00:00:00";
            endTime += " 00:00:00";


            long startLong = df.parse(startTime).getTime() / 1000;
            long tmp = startLong;
            long endLong = df.parse(endTime).getTime() / 1000;
//            startLong = endLong - 100;
//            tmp = startLong;


            while (tmp + 60 < endLong) {//每分钟只采样一次
                long end = tmp + 120;

                for (Server server : serverList) {
                    try {

                        String sql = "SELECT * FROM redis_stats WHERE curr_time > " + tmp + " AND curr_time <= " + end + " AND serverId =" + server.getId() + " LIMIT " + 2 + " OFFSET " + 0;

                        List<RedisStats> redisStatsestmp = redisStatsService.search(sql);
                        if (0 != redisStatsestmp.size()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm",Locale.ENGLISH);
                            Date nameDate = new Date(tmp * 1000);
                            String name = "Redis_" + sdf.format(nameDate) + "_" + server.getAddress();

                            RedisStats redisStatsDelta = getRedisDelta(redisStatsestmp);

                            if (null == redisStatsDelta) {
                                continue;
                            }

                            if (null == redisStatsMap.get(name)) {
                                redisStatsMap.put(name, redisStatsDelta);
                            } else {
                                RedisStats redisStats = dealRedisStats(redisStatsDelta, redisStatsMap.get(name));

                                redisStatsMap.remove(name);
                                redisStatsMap.put(name, redisStats);
                            }
                        }
                    }catch (Exception e){
                        logger.error("getRedisState:" + e);
                    }
                }
                tmp += 60;
            }

            for (Server server : serverList) {
                String sql = "SELECT * FROM redis_stats WHERE curr_time > " + tmp + " AND curr_time <= " + endLong + " AND serverId =" + server.getId() + " LIMIT " + 2 + " OFFSET " + 0;
                try {
                    List<RedisStats> redisStatsestmp = redisStatsService.search(sql);
                    if (0 != redisStatsestmp.size()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm",Locale.ENGLISH);
                        Date nameDate = new Date(tmp * 1000);
                        String name = "Redis_" + sdf.format(nameDate) + "_" + server.getAddress();

                        RedisStats redisStatsDelta = getRedisDelta(redisStatsestmp);

                        if (null == redisStatsDelta) {
                            continue;
                        }

                        if (null == redisStatsMap.get(name)) {
                            redisStatsMap.put(name, redisStatsDelta);
                        } else {
                            RedisStats redisStats = dealRedisStats(redisStatsDelta, redisStatsMap.get(name));

                            redisStatsMap.remove(name);
                            redisStatsMap.put(name, redisStats);
                        }
                    }
                } catch (Exception e) {
                    logger.error("getRedisState:" + e);
                }
            }
        }


        return redisStatsMap;

    }

    private RedisStats getRedisDelta(List<RedisStats> redisStatsestmp) {
        RedisStats redisStats = new RedisStats();

        if (redisStatsestmp.size() < 2) {
            return null;
        }

        redisStats.setInput_kbps(Math.abs(redisStatsestmp.get(1).getInput_kbps() - redisStatsestmp.get(0).getInput_kbps()));

        redisStats.setMemory_used(redisStatsestmp.get(0).getMemory_used());

        redisStats.setOutput_kbps(Math.abs(redisStatsestmp.get(1).getOutput_kbps() - redisStatsestmp.get(0).getOutput_kbps()));

        redisStats.setQps(Math.abs(redisStatsestmp.get(1).getQps() - redisStatsestmp.get(0).getQps()));

        redisStats.setTotal_connections(redisStatsestmp.get(0).getTotal_connections());

        redisStats.setUsed_cpu_sys(redisStatsestmp.get(0).getUsed_cpu_sys());

        redisStats.setUsed_cpu_sys_children(redisStatsestmp.get(0).getUsed_cpu_sys_children());

        redisStats.setUsed_cpu_user(redisStatsestmp.get(0).getUsed_cpu_user());

        redisStats.setUsed_cpu_user_children(redisStatsestmp.get(0).getUsed_cpu_user_children());

        redisStats.setId(redisStatsestmp.get(0).getId());
        redisStats.setServerId(redisStatsestmp.get(0).getServerId());
        redisStats.setCurr_time(redisStatsestmp.get(0).getCurr_time());

        return redisStats;
    }

    private RedisStats dealRedisStats(RedisStats curr, RedisStats base) {
        RedisStats redisStats = new RedisStats();

        redisStats.setConnected_clients((int) (compute(curr.getConnected_clients(), base.getConnected_clients())));

        redisStats.setInput_kbps(compute(curr.getInput_kbps(), base.getInput_kbps()));

        redisStats.setMemory_used((long) (compute(curr.getMemory_used(), base.getMemory_used())));

        redisStats.setOutput_kbps(compute(curr.getOutput_kbps(), base.getOutput_kbps()));

        redisStats.setQps((int) (compute(curr.getQps(), base.getQps())));

        redisStats.setTotal_connections((int) (compute(curr.getTotal_connections(), base.getTotal_connections())));

        redisStats.setUsed_cpu_sys(compute(curr.getUsed_cpu_sys(), base.getUsed_cpu_sys()));

        redisStats.setUsed_cpu_sys_children(compute(curr.getUsed_cpu_sys_children(), base.getUsed_cpu_sys_children()));

        redisStats.setUsed_cpu_user(compute(curr.getUsed_cpu_user(), base.getUsed_cpu_user()));

        redisStats.setUsed_cpu_user_children(compute(curr.getUsed_cpu_user_children(), base.getUsed_cpu_user_children()));

        redisStats.setId(curr.getId());
        redisStats.setServerId(curr.getServerId());
        redisStats.setCurr_time(curr.getCurr_time());


        return redisStats;
    }

    private double compute(double cur, double base) {
        if(0 == cur && 0 == base){
            return base;
        }else if(0==cur && 0 != base){
            return base;
        }else if(0!=cur && 0 == base){
            return cur;
        }else {
            return cur* 0.2 + base * 0.8;
        }

    }


    private List<String> getStartTime() {
        List<String> startList = new LinkedList<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        for (int i = 0; i < 4; i++) {
            int remainDay = 0 - 7;//一周的数据
            gc.add(5, remainDay);
            String startDateString = df.format(gc.getTime());

            startList.add(startDateString);

        }
        Collections.reverse(startList);
        return startList;
    }

    public static void main(String[] args) {
        BaselineComputeTask task = new BaselineComputeTask();
        task.run();
    }

}
