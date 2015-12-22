package com.dianping.cache.alarm.event;

import com.dianping.cache.alarm.memcache.MemcacheEvent;
import com.dianping.cache.alarm.redis.RedisEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 15/11/29.
 */
@Component
public class EventFactoryImpl implements EventFactory,ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public MemcacheEvent createMemcacheEvent() {
        MemcacheEvent memcacheEvent = (MemcacheEvent)applicationContext.getBean("memcacheEvent");
        return memcacheEvent;
    }

    @Override
    public RedisEvent createRedisEvent() {
        RedisEvent redisEvent = (RedisEvent)applicationContext.getBean("redisEvent");

        return redisEvent;
    }

}
