package com.dianping.cache.alarm.memcache;

import com.dianping.cache.entity.MemcachedStats;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.MemcachedStatsService;
import com.dianping.cache.service.ServerStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component
public class MemcacheStatsFlucService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    MemcachedStatsService memcacheStatsService;

    @Autowired
    ServerStatsService serverStatsService;


    public Float getMemcacheMemUsageByTime(int minutes, String server){
        Float usage= null;

        try {
            usage = getMemcacheMemUsage(minutes, server);

        }catch (ParseException e){
            e.printStackTrace();
        }

        return usage;
    }


    private Float getMemcacheMemUsage(int minutes, String server) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        List<String> startTimeList = getStartTime(minutes);

        String startTime = startTimeList.get(0);
        String endTime = startTimeList.get(1);

        Float memUsage = getMemcacheUsage(startTime, endTime, server);
        if(null == memUsage){
            memUsage = (float)0;
        }

        return memUsage;
    }

    private Float getMemcacheUsage(String startTime, String endTime, String server) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Map<String, MemcachedStats> memcacheStatsMap = new HashMap<String, MemcachedStats>();


        long startLong = df.parse(startTime).getTime() / 1000;
        long tmp = startLong;
        long endLong = df.parse(endTime).getTime() / 1000;

        List<ServerStats> serverStatses = serverStatsService.findByServerWithInterval(server,startLong,endLong);

        if((serverStatses.size()>0) && (serverStatses.get(0).getMem_total())!=0){
            long memUsed = serverStatses.get(0).getMem_used();
            long memTotal = serverStatses.get(0).getMem_total();

            Float usage = (float)memUsed/memTotal;

            return usage;
        }

        return null;
    }



    public MemcachedStats getMemcacheStatsByTime(int minutes, int serverId) {
        MemcachedStats memcachedStats = null;
        try {

            memcachedStats = getMemcacheStats(minutes, serverId);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return memcachedStats;
    }


    private MemcachedStats getMemcacheStats(int minutes, int serverId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        List<String> startTimeList = getStartTime(minutes);

        String startTime = startTimeList.get(0);
        String endTime = startTimeList.get(1);

        MemcachedStats memcacheStats = getMemcacheState(startTime, endTime, serverId);
        return memcacheStats;
    }



    private MemcachedStats getMemcacheState(String startTime, String endTime, int serverId) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Map<String, MemcachedStats> memcacheStatsMap = new HashMap<String, MemcachedStats>();


        long startLong = df.parse(startTime).getTime() / 1000;
        long tmp = startLong;
        long endLong = df.parse(endTime).getTime() / 1000;


        while (tmp + 60 < endLong) {//每分钟只采样一次
            long end = tmp + 120;


            String sql = "SELECT id,serverId, uptime, curr_time, total_conn, curr_conn, curr_items, cmd_set, get_hits, get_misses," +
                    " bytes_read, bytes_written, delete_hits, delete_misses, evictions,limit_maxbytes, bytes " +
                    "FROM memcache_stats " +
                    " WHERE curr_time > " + tmp + " AND curr_time <= " + end + " AND serverId =" + serverId;

            List<MemcachedStats> memcacheStatsestmp = memcacheStatsService.search(sql);
            if (1 < memcacheStatsestmp.size()) {

                MemcachedStats memcacheStatsDelta = getMemcacheDelta(memcacheStatsestmp);

                if (null == memcacheStatsDelta) {
                    continue;
                }

                return memcacheStatsDelta;
            }
            tmp+=60;
        }

        return null;

    }

    private MemcachedStats getMemcacheDelta(List<MemcachedStats> memcacheStatsestmp) {

        MemcachedStats memcacheStats = new MemcachedStats();
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

        memcacheStats.setCurr_items(memcacheStatsestmp.get(0).getCurr_items());

        memcacheStats.setGet_hits(Math.abs(memcacheStatsestmp.get(1).getGet_hits() - memcacheStatsestmp.get(0).getGet_hits()));

        memcacheStats.setGet_misses(Math.abs(memcacheStatsestmp.get(1).getGet_misses() - memcacheStatsestmp.get(0).getGet_misses()));

        memcacheStats.setLimit_maxbytes(memcacheStatsestmp.get(0).getLimit_maxbytes());

        memcacheStats.setTotal_conn(memcacheStatsestmp.get(0).getTotal_conn());

        return memcacheStats;
    }

    private List<String> getStartTime(int minutes) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());

        int remainMinutes = 0 - minutes;
        gc.add(12, remainMinutes);
        String startDateString = df.format(gc.getTime());

        gc.add(12, 4);
        String endDateString = df.format(gc.getTime());

        List<String> startTime = new ArrayList<String>();
        startTime.add(startDateString);
        startTime.add(endDateString);

        return startTime;
    }

}
