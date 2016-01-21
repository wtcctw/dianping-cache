package com.dianping.squirrel.monitor.collector;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.cluster.redis.RedisCluster;
import com.dianping.cache.scale.cluster.redis.RedisInfo;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import com.dianping.cache.service.ServerService;
import com.dianping.squirrel.monitor.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisStatsCollector extends AbstractCollector {
    private static final Logger logger = LoggerFactory.getLogger(RedisStatsCollector.class);

    @Autowired
    private ServerService serverService;

    @Scheduled(cron = "5/30 * * * * *")
    public void scheduled(){
        if(isLeader() && isProductEnv()){
            for(Map.Entry<String,RedisCluster> value : RedisManager.getClusterCache().entrySet()){
			    for(RedisServer server : value.getValue().getAllAliveServer()){
                    Data data = collectData(server);
                    dataManager.addData(data);
                }
		    }
        }
    }

    private Data collectData(RedisServer server){
        Server server1 = serverService.findByAddress(server.getAddress());
        if(server1 == null){
            return null;
        }
        Data data = new Data();
        data.setType(Data.DataType.RedisStats);
        RedisStats stats;
        try {
            stats = translate(server.getInfo());
            stats.setServerId(server1.getId());
        } catch (Exception e) {
            logger.error("collect redis stats failed : ",e);
            return null;
        }
        data.setStats(stats);
        return data;
    }

    private RedisStats translate(RedisInfo info){
        RedisStats stat = new RedisStats();
        stat.setCurr_time(System.currentTimeMillis()/1000);
        stat.setMemory_used(info.getUsedMemory());
        stat.setTotal_connections(info.getTotal_connections());
        stat.setConnected_clients(info.getConnected_clients());
        stat.setInput_kbps(info.getInput_kbps());
        stat.setOutput_kbps(info.getOutput_kbps());
        stat.setQps(info.getQps());
        stat.setUsed_cpu_sys(info.getUsed_cpu_sys());
        stat.setUsed_cpu_sys_children(info.getUsed_cpu_sys_children());
        stat.setUsed_cpu_user(info.getUsed_cpu_user());
        stat.setUsed_cpu_user_children(info.getUsed_cpu_user_children());
        return stat;
    }

}
