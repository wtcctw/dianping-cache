package com.dianping.cache.alarm.dataanalyse.mapper;

import com.dianping.cache.alarm.entity.RedisBaseline;
import com.dianping.cache.entity.RedisStats;

import java.util.Date;

/**
 * Created by lvshiyun on 16/1/4.
 */
public class RedisBaselineMapper {

    public static RedisBaseline convertToRedisBaseline(RedisStats redisStats) {
        RedisBaseline redisBaseline = new RedisBaseline();

        redisBaseline.setServerId(redisStats.getServerId())
                .setConnected_clients(redisStats.getConnected_clients())
                .setCurr_time(redisStats.getCurr_time())
                .setInput_kbps(redisStats.getInput_kbps())
                .setMemory_used(redisStats.getMemory_used())
                .setOutput_kbps(redisStats.getOutput_kbps())
                .setQps(redisStats.getQps())
                .setTotal_connections(redisStats.getTotal_connections())
                .setUsed_cpu_sys(redisStats.getUsed_cpu_sys())
                .setUsed_cpu_sys_children(redisStats.getUsed_cpu_sys_children())
                .setUsed_cpu_user(redisStats.getUsed_cpu_user())
                .setUsed_cpu_user_children(redisStats.getUsed_cpu_user_children())
                .setUpdateTime(new Date());

        return redisBaseline;

    }
}
