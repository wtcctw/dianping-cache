package com.dianping.cache.alarm.dataanalyse.baselineCache;

import com.dianping.cache.alarm.dataanalyse.service.MemcacheBaselineService;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/26.
 */
@Component
public class BaselineCacheServiceImpl implements BaselineCacheService {

    @Autowired
    MemcacheBaselineService memcacheBaselineService;

    @Autowired
    RedisBaselineService redisBaselineService;

    private Map<String, MemcacheBaseline> memcacheBaselineMap;

    private Map<String, RedisBaseline> redisBaselineMap;


    public BaselineCacheServiceImpl() {
        memcacheBaselineMap = new HashMap<String, MemcacheBaseline>();
        redisBaselineMap = new HashMap<String, RedisBaseline>();
    }


    @Override
    public MemcacheBaseline getMemcacheBaselineByName(String name) {
        return this.memcacheBaselineMap.get(name);
    }

    @Override
    public RedisBaseline getRedisBaselineByName(String name) {
        return this.redisBaselineMap.get(name);
    }

    @Override
    public synchronized void reload() {
        Map<String, MemcacheBaseline> memcacheBaselineMap = getMemcacheBaselineFromDb();

        putMemcacheBaselineMapToCache(memcacheBaselineMap);

        Map<String, RedisBaseline> redisBaselineMap = getRedisBaselineFromDb();

        putRedisBaselineMapToCache(redisBaselineMap);
    }

    @Override
    public void putRedisBaselineMapToCache(Map<String, RedisBaseline> redisBaselineMap) {

        Map<String, RedisBaseline> baselineMap = new HashMap<String, RedisBaseline>(redisBaselineMap);

        this.redisBaselineMap = baselineMap;
    }

    @Override
    public Map<String, RedisBaseline> getRedisBaselineFromDb() {

        Map<String, RedisBaseline> redisBaselineMap = new HashMap<String, RedisBaseline>();

        List<RedisBaseline> redisBaselines = redisBaselineService.findAll();

        for (RedisBaseline redisBaseline : redisBaselines) {
            redisBaselineMap.put(redisBaseline.getBaseline_name(), redisBaseline);
        }

        return redisBaselineMap;
    }

    @Override
    public void putMemcacheBaselineMapToCache(Map<String, MemcacheBaseline> memcacheBaselineMap) {

        Map<String, MemcacheBaseline> baselineMap = new HashMap<String, MemcacheBaseline>(memcacheBaselineMap);

        this.memcacheBaselineMap = baselineMap;

    }

    @Override
    public Map<String, MemcacheBaseline> getMemcacheBaselineFromDb() {
        Map<String, MemcacheBaseline> memcacheBaselineMap = new HashMap<String, MemcacheBaseline>();

        List<MemcacheBaseline> memcacheBaselines = memcacheBaselineService.findAll();

        for (MemcacheBaseline memcacheBaseline : memcacheBaselines) {
            memcacheBaselineMap.put(memcacheBaseline.getBaseline_name(), memcacheBaseline);
        }

        return memcacheBaselineMap;
    }


}
