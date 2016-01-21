package com.dianping.squirrel.monitor.collector;

import com.dianping.cache.entity.MemcachedStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.monitor.MemcachedClientFactory;
import com.dianping.cache.service.ServerService;
import com.dianping.squirrel.monitor.data.Data;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class MemcachedStatsCollector extends AbstractCollector {

    private static final Logger logger = LoggerFactory.getLogger(MemcachedStatsCollector.class);

    @Autowired
    private ServerService serverService;

    @Scheduled(cron = "0/30 * * * * *")
    public void scheduled(){
        if(isLeader()){
            List<Server> serverlist = serverService.findAllMemcachedServers();
            for(Server server : serverlist){
                Data data = collectData(server);
                dataManager.addData(data);
            }
        }
    }


    private Data collectData(Server server){
        Data data = new Data();
        data.setType(Data.DataType.MemcachedStats);
        try {
            MemcachedClient client = MemcachedClientFactory.getInstance().getClient(server.getAddress());
            String[] address = server.getAddress().split(":");
            String ip = address[0];
            int port = address.length > 1 ? Integer.parseInt(address[1]) : 11211;
            MemcachedStats stats = translate(client.getStats().get(new InetSocketAddress(ip,port)));
            stats.setServerId(server.getId());
            data.setStats(stats);
        }catch (Exception e){
            //logger.error("collect memcached stats failed : ",e);
            return null;
        }
        return data;
    }

    private MemcachedStats translate(Map<String,String> stats){
        checkNotNull(stats,"stats is null");
        MemcachedStats msData = new MemcachedStats();
        msData.setUptime(Integer.parseInt(stats.get("uptime")));
        msData.setCurr_time(System.currentTimeMillis()/1000);
        msData.setTotal_conn(Integer.parseInt(stats.get("total_connections")));
        msData.setCurr_conn(Integer.parseInt(stats.get("curr_connections")));
        msData.setCurr_items(Integer.parseInt(stats.get("curr_items")));
        msData.setCmd_set(Long.parseLong(stats.get("cmd_set")));
        msData.setGet_hits(Long.parseLong(stats.get("get_hits")));
        msData.setGet_misses(Long.parseLong(stats.get("get_misses")));
        msData.setLimit_maxbytes(Long.parseLong(stats.get("limit_maxbytes")));
        msData.setDelete_hits(Long.parseLong(stats.get("delete_hits")));
        msData.setDelete_misses(Long.parseLong(stats.get("delete_misses")));
        msData.setEvictions(Long.parseLong(stats.get("evictions")));
        msData.setBytes_read(Long.parseLong(stats.get("bytes_read")));
        msData.setBytes_written(Long.parseLong(stats.get("bytes_written")));
        msData.setBytes(Long.parseLong(stats.get("bytes")));
        return msData;
    }

}
