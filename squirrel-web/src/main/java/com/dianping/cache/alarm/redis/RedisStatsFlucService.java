package com.dianping.cache.alarm.redis;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.RedisService;
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
public class RedisStatsFlucService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RedisService redisService;


    @Autowired
    ServerStatsService serverStatsService;


    public Float getRedisMemUsageByTime(int minutes, String server){
        Float usage= null;

        try {
            usage = getRedisMemUsage(minutes, server);

        }catch (ParseException e){
            e.printStackTrace();
        }

        return usage;
    }


    private Float getRedisMemUsage(int minutes, String server) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        List<String> startTimeList = getStartTime(minutes);

        String startTime = startTimeList.get(0);
        String endTime = startTimeList.get(1);

        Float memUsage = getRedisMemUsage(startTime, endTime, server);

        return memUsage;
    }

    private Float getRedisMemUsage(String startTime, String endTime, String server) throws ParseException {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);


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



    public RedisStats getRedisStatsByTime(int minutes, int serverId) {
        RedisStats redisStats = null;
        try {

            redisStats = getRedisStats(minutes, serverId);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return redisStats;
    }


    private RedisStats getRedisStats(int minutes, int serverId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        List<String> startTimeList = getStartTime(minutes);

        String startTime = startTimeList.get(0);
        String endTime = startTimeList.get(1);

        RedisStats redisStats = getRedisState(startTime, endTime, serverId);
        return redisStats;
    }




    private RedisStats getRedisState(String startTime, String endTime, int serverId) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);


        long startLong = df.parse(startTime).getTime() / 1000;
        long tmp = startLong;
        long endLong = df.parse(endTime).getTime() / 1000;


        while (tmp + 60 < endLong) {//每分钟只采样一次
            long end = tmp + 120;

            try {

                String sql = "SELECT * FROM redis_stats WHERE curr_time > " + tmp + " AND curr_time <= " + end + " AND serverId =" + serverId + " LIMIT " + 2 + " OFFSET " + 0;

                List<RedisStats> redisStatstmp = redisService.search(sql);
                if (1 < redisStatstmp.size()) {

                    RedisStats redisStatsDelta = getRedisDelta(redisStatstmp);

                    if (null == redisStatsDelta) {
                        continue;
                    }

                    return redisStatsDelta;
                }
            } catch (Exception e) {
                logger.error("getRedisState:" + e);
            }

            tmp += 60;
        }


        return null;

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
