package com.dianping.cache.alarm.dataanalyse.baselineCache;

import com.dianping.cache.alarm.dataanalyse.service.BaselineComputeTaskService;
import com.dianping.cache.alarm.dataanalyse.service.MemcacheBaselineService;
import com.dianping.cache.alarm.dataanalyse.service.RedisBaselineService;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/26.
 */
@Component
public class BaselineCacheServiceImpl implements BaselineCacheService {

    @Autowired
    MemcacheBaselineService memcacheBaselineService;

    @Autowired
    RedisBaselineService redisBaselineService;

    @Autowired
    BaselineComputeTaskService baselineComputeTaskService;

    private BaselineCache baselineCache;

    public BaselineCacheServiceImpl() {
        baselineCache = BaselineCache.getInstance();
    }

    @Override
    public MemcacheBaseline getMemcacheBaselineByName(String name) {
        return baselineCache.getFromMemcacheBaselineMap(name);
    }

    @Override
    public RedisBaseline getRedisBaselineByName(String name) {
        return baselineCache.getFromRedisBaselineMap(name);
    }

    @Override
    public void putBaselineMapToCache(Map<String, MemcacheBaseline> memcacheBaselineMap,Map<String, RedisBaseline> redisBaselineMap) {

        Map<String,MemcacheBaseline>stringMemcacheBaselineMap = new HashMap<String, MemcacheBaseline>(memcacheBaselineMap);

        Map<String, RedisBaseline> stringRedisBaselineMap = new HashMap<String, RedisBaseline>(redisBaselineMap);


        BaselineCache baselineCache = new BaselineCache();

        baselineCache.setMemcacheBaselineMap(stringMemcacheBaselineMap);

        baselineCache.setRedisBaselineMap(stringRedisBaselineMap);


        BaselineCache.setInstance(baselineCache);

    }

    @Override
    public Map<String, RedisBaseline> getRedisBaselineFromDb(Date date) {

        Map<String, RedisBaseline> redisBaselineMap = new HashMap<String, RedisBaseline>();

        int taskId = baselineComputeTaskService.getRecentTaskId().get(0).getId();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH", Locale.ENGLISH);

        String nameNow = "Redis_" + sdf.format(date);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.add(12,60);
        String nameHourLater = "Redis_" + sdf.format(gc.getTime());
        gc.add(12, -120);//1小时之前
        String nameHourAgo = "Redis_" +sdf.format(gc.getTime());

        String sql = "SELECT * FROM redis_baseline WHERE baseline_name LIKE '"+ nameHourLater +"%' OR baseline_name LIKE '" + nameNow + "%' OR baseline_name LIKE '"+nameHourAgo + "%' AND taskId =" + taskId;

        List<RedisBaseline> redisBaselines = redisBaselineService.search(sql);

        for (RedisBaseline redisBaseline : redisBaselines) {
            redisBaselineMap.put(redisBaseline.getBaseline_name(), redisBaseline);
        }

        return redisBaselineMap;
    }

    @Override
    public Map<String, MemcacheBaseline> getMemcacheBaselineFromDb(Date date) {
        Map<String, MemcacheBaseline> memcacheBaselineMap = new HashMap<String, MemcacheBaseline>();

        int taskId = baselineComputeTaskService.getRecentTaskId().get(0).getId();


        SimpleDateFormat sdf = new SimpleDateFormat("EEEE:HH", Locale.ENGLISH);

        String nameNow = "Memcache_" + sdf.format(date);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.add(12, 60);
        String nameHourLater = "Memcache_" + sdf.format(gc.getTime());
        gc.add(12, -120);//1小时之前
        String nameHourAgo = "Memcache_" +sdf.format(gc.getTime());


        String sql = "SELECT * FROM memcache_baseline WHERE baseline_name LIKE '"+nameHourLater+"%' OR baseline_name LIKE '" + nameNow + "%' OR baseline_name LIKE '"+nameHourAgo +"%' AND taskId =" + taskId;



        List<MemcacheBaseline> memcacheBaselines = memcacheBaselineService.search(sql);

        for (MemcacheBaseline memcacheBaseline : memcacheBaselines) {
            memcacheBaselineMap.put(memcacheBaseline.getBaseline_name(), memcacheBaseline);
        }

        return memcacheBaselineMap;
    }

}
