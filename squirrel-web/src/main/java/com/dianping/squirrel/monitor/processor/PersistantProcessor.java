package com.dianping.squirrel.monitor.processor;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.MemcachedStatsService;
import com.dianping.cache.service.RedisService;
import com.dianping.cache.service.ServerStatsService;
import com.dianping.squirrel.monitor.data.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersistantProcessor extends AbstractProcessor {

    @Autowired
    private MemcachedStatsService memcachedStatsService;

    @Autowired
    private ServerStatsService serverStatsService;

    @Autowired
    private RedisService redisService;


    @Override
    public void process(Data data) {
        switch (data.getType()){
            case MemcachedStats:
                //memcachedStatsService.insert((MemcachedStats) data.getStats());
                break;
            case MemcachedHeartbeat:
                break;
            case ZabbixStats:
                serverStatsService.insert((ServerStats) data.getStats());
                break;
            case RedisStats:
                redisService.insert((RedisStats) data.getStats());
                break;
            case CatStats:
                break;
        }
    }

    @Override
    public List<String> getType() {
        return new ArrayList<String>(){{
            add(Data.DataType.MemcachedStats.toString());
            add(Data.DataType.RedisStats.toString());
            add(Data.DataType.ZabbixStats.toString());
            //add(Data.DataType.CatStats.toString());
        }};
    }
}
