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
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.RedisStatsService;
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


    public void run() {

        try {
            Date now = new Date();
            com.dianping.cache.alarm.entity.BaselineComputeTask baselinecomputeTask = new com.dianping.cache.alarm.entity.BaselineComputeTask();
            baselinecomputeTask.setCreateTime(now);

            baselineComputeTaskService.insert(baselinecomputeTask);

            int taskId = baselineComputeTaskService.getRecentTaskId().get(0).getId();


//            memcacheBaselineCompute(taskId);

            redisBaselineCompute(taskId);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    private void memcacheBaselineCompute(int taskId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<String> startTime = getStartTime();
        String endTime = df.format(new Date());

        Map<String, MemcacheStats> memcacheStatses = getMemcacheState(startTime, endTime);

        memcacheBaslineStoreToDb(taskId,memcacheStatses);


    }

    private void memcacheBaslineStoreToDb(int taskId, Map<String, MemcacheStats> memcacheStatses) {

        for (Map.Entry<String, MemcacheStats> entry : memcacheStatses.entrySet()) {
            MemcacheBaseline memcacheBaseline = MemcacheBaselineMapper.convertToMemcacheBaseline(entry.getValue());
            memcacheBaseline.setBaseline_name(entry.getKey());
            memcacheBaseline.setTaskId(taskId);

            memcacheBaselineService.insert(memcacheBaseline);

        }
    }

    private void redisBaselineCompute(int taskId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        List<String> startTime = getStartTime();
        String endTime = df.format(new Date());

        Map<String, RedisStats> redisStatsMap = getRedisState(startTime, endTime);

        redisBaslineStoreToDb(taskId,redisStatsMap);

    }

    private void redisBaslineStoreToDb(int taskId,Map<String, RedisStats> redisStatsMap) {

        for (Map.Entry<String, RedisStats> entry : redisStatsMap.entrySet()) {
            RedisBaseline redisBaseline = RedisBaselineMapper.convertToRedisBaseline(entry.getValue());
            redisBaseline.setBaseline_name(entry.getKey());
            redisBaseline.setTaskId(taskId);
            redisBaselineService.insert(redisBaseline);

        }
    }


    private Map<String, MemcacheStats> getMemcacheState(List<String> startTimeList, String endDate) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, MemcacheStats> memcacheStatsMap = new HashMap<String, MemcacheStats>();

        for (int i = 0; i < startTimeList.size(); i++) {
            String startTime = startTimeList.get(i);
            startTime = startTime.split(" ")[0];
            String endTime;
            if (i < startTimeList.size()-1) {
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



            while (tmp + 60 < endLong) {//每分钟只采样一次
                long end = tmp + 60;

                Date s = new Date(tmp);
                Date e = new Date(end);

                String sql = "SELECT id,serverId, uptime, curr_time, total_conn, curr_conn, curr_items, cmd_set, get_hits, get_misses," +
                        " bytes_read, bytes_written, delete_hits, delete_misses, evictions,limit_maxbytes, bytes " +
                        "FROM memcache_stats " +
                        " WHERE curr_time > " + tmp + " AND curr_time < " + end;

                List<MemcacheStats> memcacheStatsestmp = memcacheStatsService.search(sql);
                if (0 != memcacheStatsestmp.size()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm");
                    Date nameDate = new Date(tmp*1000);
                    String name = "Memcache_" + sdf.format(nameDate);
                    if (null == memcacheStatsMap.get(name)) {
                        memcacheStatsMap.put(name, memcacheStatsestmp.get(0));
                    } else {
                        MemcacheStats memcacheStats = dealMemcacheStats(memcacheStatsestmp.get(0), memcacheStatsMap.get(name));

                        memcacheStatsMap.remove(name);
                        memcacheStatsMap.put(name, memcacheStats);
                    }
                }
                tmp += 60;
            }

            String sql = "SELECT id,serverId, uptime, curr_time, total_conn, curr_conn, curr_items, cmd_set, get_hits, get_misses," +
                    " bytes_read, bytes_written, delete_hits, delete_misses, evictions,limit_maxbytes, bytes " +
                    "FROM memcache_stats " +
                    " WHERE curr_time > " + tmp + " AND curr_time < " + endLong;
            List<MemcacheStats> memcacheStatsestmp = memcacheStatsService.search(sql);
            if (0 != memcacheStatsestmp.size()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm");
                Date nameDate = new Date(tmp*1000);
                String name = "Memcache_" + sdf.format(nameDate);
                if (null == memcacheStatsMap.get(name)) {
                    memcacheStatsMap.put(name, memcacheStatsestmp.get(0));
                } else {
                    MemcacheStats memcacheStats = dealMemcacheStats(memcacheStatsestmp.get(0), memcacheStatsMap.get(name));

                    memcacheStatsMap.remove(name);
                    memcacheStatsMap.put(name, memcacheStats);
                }
            }
        }


        return memcacheStatsMap;

    }

    private MemcacheStats dealMemcacheStats(MemcacheStats curr, MemcacheStats base) {
        MemcacheStats memcacheStats = new MemcacheStats();

        memcacheStats.setId(curr.getId());
        memcacheStats.setServerId(curr.getServerId());
        memcacheStats.setCurr_time(curr.getCurr_time());

        memcacheStats.setBytes((long) (curr.getBytes() * 0.2 + base.getBytes() * 0.8));

        memcacheStats.setBytes_read((long) (curr.getBytes_read() * 0.2 + base.getBytes_read() * 0.8));

        memcacheStats.setBytes_written((long) (curr.getBytes_written() * 0.2 + base.getBytes_written() * 0.8));

        memcacheStats.setCmd_set((long) (curr.getCmd_set() * 0.2 + base.getCmd_set() * 0.8));

        memcacheStats.setCurr_conn((int) (curr.getCurr_conn() * 0.2 + base.getCurr_conn() * 0.8));

        memcacheStats.setDelete_hits((long) (curr.getDelete_hits() * 0.2 + base.getDelete_hits() * 0.8));

        memcacheStats.setEvictions((long) (curr.getEvictions() * 0.2 + base.getEvictions() * 0.8));

        memcacheStats.setDelete_misses((long) (curr.getDelete_misses() * 0.2 + base.getDelete_misses() * 0.8));

        memcacheStats.setCurr_items((int) (curr.getCurr_items() * 0.2 + base.getCurr_items() * 0.8));

        memcacheStats.setGet_hits((long) (curr.getGet_hits() * 0.2 + base.getGet_hits() * 0.8));

        memcacheStats.setGet_misses((long) (curr.getGet_misses() * 0.2 + base.getGet_misses() * 0.8));

        memcacheStats.setLimit_maxbytes((long) (curr.getLimit_maxbytes() * 0.2 + base.getLimit_maxbytes() * 0.8));

        memcacheStats.setTotal_conn((int) (curr.getTotal_conn() * 0.2 + base.getTotal_conn() * 0.8));

        return memcacheStats;

    }

    private Map<String, RedisStats> getRedisState(List<String> startTimeList, String endDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<String, RedisStats> redisStatsMap = new HashMap<String, RedisStats>();

        for (int i = 0; i < startTimeList.size(); i++) {
            String startTime = startTimeList.get(i);
            startTime = startTime.split(" ")[0];
            String endTime;
            if (i < startTimeList.size()) {
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


            while (tmp + 60 < endLong) {//每分钟只采样一次
                long end = tmp + 60;

                String sql = "SELECT * FROM redis_stats WHERE curr_time > " + tmp + " AND curr_time < " + end;

                List<RedisStats> redisStatsestmp = redisStatsService.search(sql);
                if (0 != redisStatsestmp.size()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm");
                    Date nameDate = new Date(tmp*1000);
                    String name = "Redis_" + sdf.format(nameDate);
                    if (null == redisStatsMap.get(name)) {
                        redisStatsMap.put(name, redisStatsestmp.get(0));
                    } else {
                        RedisStats redisStats = dealRedisStats(redisStatsestmp.get(0), redisStatsMap.get(name));

                        redisStatsMap.remove(name);
                        redisStatsMap.put(name, redisStats);
                    }
                }
                tmp += 60;
            }

            String sql = "SELECT * FROM redis_stats WHERE curr_time > " + tmp + " AND curr_time < " + endLong;
            List<RedisStats> redisStatsestmp = redisStatsService.search(sql);
            if (0 != redisStatsestmp.size()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH:mm");
                Date nameDate = new Date(tmp*1000);
                String name = "Redis_" + sdf.format(nameDate);
                if (null == redisStatsMap.get(name)) {
                    redisStatsMap.put(name, redisStatsestmp.get(0));
                } else {
                    RedisStats redisStats = dealRedisStats(redisStatsestmp.get(0), redisStatsMap.get(name));

                    redisStatsMap.remove(name);
                    redisStatsMap.put(name, redisStats);
                }
            }
        }


        return redisStatsMap;

    }

    private RedisStats dealRedisStats(RedisStats curr, RedisStats base) {
        RedisStats redisStats = new RedisStats();

        redisStats.setConnected_clients((int) (curr.getConnected_clients() * 0.2 + base.getConnected_clients() * 0.8));

        redisStats.setInput_kbps(curr.getInput_kbps() * 0.2 + base.getInput_kbps() * 0.8);

        redisStats.setMemory_used((long) (curr.getMemory_used() * 0.2 + base.getMemory_used() * 0.8));

        redisStats.setOutput_kbps(curr.getOutput_kbps() * 0.2 + base.getOutput_kbps() * 0.8);

        redisStats.setQps((int) (curr.getQps() * 0.2 + base.getQps() * 0.8));

        redisStats.setTotal_connections((int) (curr.getTotal_connections() * 0.2 + base.getTotal_connections() * 0.8));

        redisStats.setUsed_cpu_sys(curr.getUsed_cpu_sys() * 0.2 + base.getUsed_cpu_sys() * 0.8);

        redisStats.setUsed_cpu_sys_children(curr.getUsed_cpu_sys_children() * 0.2 + base.getUsed_cpu_sys_children() * 0.8);

        redisStats.setUsed_cpu_user(curr.getUsed_cpu_user() * 0.2 + base.getUsed_cpu_user() * 0.8);

        redisStats.setUsed_cpu_user_children(curr.getUsed_cpu_user_children() * 0.2 + base.getUsed_cpu_user_children() * 0.8);

        redisStats.setId(curr.getId());
        redisStats.setServerId(curr.getServerId());
        redisStats.setCurr_time(curr.getCurr_time());


        return redisStats;
    }


    private List<String> getStartTime() {
        List<String> startList = new LinkedList<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
