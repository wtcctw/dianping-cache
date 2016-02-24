package com.dianping.cache.alarm.dataanalyse.task;

import com.dianping.cache.alarm.dataanalyse.baselineCache.BaselineCacheService;
import com.dianping.cache.alarm.entity.MemcacheBaseline;
import com.dianping.cache.alarm.entity.RedisBaseline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineMapGetTask")
@Scope("prototype")
public class BaselineMapGetTask {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    BaselineCacheService baselineCacheService;

    public void run() {

        //获取当前时间之前5分钟，之后1小时的baseline数据，放入map中
        Date now = new Date();
        logger.info("baselineMapGetTask Time:"+now.toString(),getClass());

        Map<String, MemcacheBaseline> memcacheBaselineMap = baselineCacheService.getMemcacheBaselineFromDb(now);

        Map<String, RedisBaseline> redisBaselineMap = baselineCacheService.getRedisBaselineFromDb(now);

        baselineCacheService.putBaselineMapToCache(memcacheBaselineMap, redisBaselineMap);

    }


}
