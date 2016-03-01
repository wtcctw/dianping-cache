package com.dianping.cache.alarm.dataanalyse.baselineCache;

import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvshiyun on 16/2/19.
 */
public class BaselineCache {

    private static Map<String,MemcacheBaseline> memcacheBaselineMap = new HashMap<String, MemcacheBaseline>();

    private static Map<String,RedisBaseline> redisBaselineMap = new HashMap<String, RedisBaseline>();

    private static BaselineCache INSTANCE = new BaselineCache();

    public static BaselineCache getInstance(){
        return INSTANCE;
    }

    public static void setInstance(BaselineCache baselineCache){
        INSTANCE = baselineCache;
    }

    void putToMemcacheBaselineMap(String name, MemcacheBaseline memcacheBaseline){
        memcacheBaselineMap.put(name,memcacheBaseline);
    }

    MemcacheBaseline getFromMemcacheBaselineMap(String name){
        return memcacheBaselineMap.get(name);
    }


    void putToRedisBaselineMap(String name, RedisBaseline redisBaseline){
        redisBaselineMap.put(name, redisBaseline);
    }

    RedisBaseline getFromRedisBaselineMap(String name){
        return redisBaselineMap.get(name);
    }

    public static Map<String, MemcacheBaseline> getMemcacheBaselineMap() {
        return memcacheBaselineMap;
    }

    public static void setMemcacheBaselineMap(Map<String, MemcacheBaseline> memcacheBaselineMap) {
        BaselineCache.memcacheBaselineMap = new HashMap<String, MemcacheBaseline>(memcacheBaselineMap);
    }

    public static Map<String, RedisBaseline> getRedisBaselineMap() {
        return redisBaselineMap;
    }

    public static void setRedisBaselineMap(Map<String, RedisBaseline> redisBaselineMap) {
        BaselineCache.redisBaselineMap = new HashMap<String, RedisBaseline>(redisBaselineMap);
    }
}
