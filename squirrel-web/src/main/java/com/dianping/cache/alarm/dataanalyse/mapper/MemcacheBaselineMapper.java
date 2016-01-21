package com.dianping.cache.alarm.dataanalyse.mapper;

import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.entity.MemcachedStats;

import java.util.Date;

/**
 * Created by lvshiyun on 16/1/4.
 */
public class MemcacheBaselineMapper {

    public static MemcacheBaseline convertToMemcacheBaseline(MemcachedStats memcacheStats) {
        MemcacheBaseline memcacheBaseline = new MemcacheBaseline();

        memcacheBaseline.setGet_hits(memcacheStats.getGet_hits())
                .setLimit_maxbytes(memcacheStats.getLimit_maxbytes())
                .setTotal_conn(memcacheStats.getTotal_conn())
                .setGet_misses(memcacheStats.getGet_misses())
                .setBytes(memcacheStats.getBytes())
                .setBytes_read(memcacheStats.getBytes_read())
                .setBytes_written(memcacheStats.getBytes_written())
                .setCmd_set(memcacheStats.getCmd_set())
                .setCurr_conn(memcacheStats.getCurr_conn())
                .setCurr_items(memcacheStats.getCurr_items())
                .setCurr_time(memcacheStats.getCurr_time())
                .setDelete_hits(memcacheStats.getDelete_hits())
                .setDelete_misses(memcacheStats.getDelete_misses())
                .setEvictions(memcacheStats.getEvictions())
                .setServerId(memcacheStats.getServerId())
                .setUptime(memcacheStats.getUptime())
                .setTotal_conn(memcacheStats.getTotal_conn())
                .setUpdateTime(new Date());

        return memcacheBaseline;

    }
}
