package com.dianping.cache.alarm.memcache;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.MemcacheStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.monitor.statsdata.MemcachedStatsData;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by lvshiyun on 15/11/26.
 */
public class MemcacheData {

    private ServerService serverService;

    private CacheConfigurationService cacheConfigurationService;

    private MemcacheStatsService memcacheStatsService;

    public MemcacheData() {

    }

    public List<HashMap<String, Object>> doGetMemcacheData() {


        List<CacheConfiguration> configList = cacheConfigurationService.findAll();
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        Map<String, Map<String, Object>> currentServerStats = this.getCurrentServerStatsData();

        for (CacheConfiguration item : configList) {
            //遍历所有的集群  对于集群名称为memcached的进行监控
            if (item.getCacheKey().contains("memcached")
                    && !"memcached-leo".equals(item.getCacheKey())) {
                HashMap<String, Object> info = new HashMap<String, Object>();
                info.put("key", item.getCacheKey());
                List<String> serverList = item.getServerList();

                long mem = 0, memused = 0;
                long qpsmax = Long.MIN_VALUE, qpsmin = Long.MAX_VALUE, qpsavg = 0;
                long evictmax = Long.MIN_VALUE, evictmin = Long.MAX_VALUE, evictavg = 0;
                float hitmax = -1, hitmin = 1, hitavg = 0;
                int connmax = Integer.MIN_VALUE, connmin = Integer.MAX_VALUE, connavg = 0;

                int alarm = 3;
                int alive = 0;
                for (String server : serverList) {
                    if (currentServerStats.get(server) != null) {
                        alive++;
                        mem += (Long) currentServerStats.get(server).get("max_memory");
                        memused += (Long) currentServerStats.get(server).get("used_memory");
                        long currqps = (Long) currentServerStats.get(server).get("QPS");
                        qpsavg += currqps;
                        qpsmax = Math.max(qpsmax, currqps);
                        qpsmin = Math.min(qpsmin, currqps);

                        float currhit = (Float) currentServerStats.get(server).get("hitrate");
                        hitavg += currhit;
                        hitmax = Math.max(hitmax, currhit);
                        hitmin = Math.min(hitmin, currhit);

                        int currConn = (Integer) currentServerStats.get(server).get("curr_conn");
                        connavg += currConn;
                        connmax = Math.max(connmax, currConn);
                        connmin = Math.min(connmin, currConn);

                        long currEvict = (Long) currentServerStats.get(server).get("evict");
                        evictavg += currEvict;
                        evictmax = Math.max(evictmax, currEvict);
                        evictmin = Math.min(evictmin, currEvict);

                    } else {
                        alarm = 1;
                    }
                }
                float usage = (float) memused / mem;
                if (usage > 0.7)
                    alarm = 2;
                if (hitmin < 0.97)
                    alarm = 1;
                if (alive > 0) {
                    qpsavg = qpsavg / alive;
                    hitavg = hitavg / alive;
                    connavg = connavg / alive;
                    evictavg = evictavg / alive;
                } else {
                    qpsavg = 0;
                    hitavg = 0;
                    connavg = 0;
                    evictavg = 0;
                }
                info.put("numbers", serverList.size() - alive + "/" + serverList.size());
                info.put("memory", (float) (Math.round(usage * 100)) + "%/" + mem + "M");
                info.put("QPS", qpsmax + "/" + qpsmin + "/" + qpsavg);
                info.put("hitrate", (float) (Math.round(hitmax * 100)) / 100 + "/" + (float) (Math.round(hitmin * 100)) / 100 + "/" + (float) (Math.round(hitavg * 100)) / 100);
                info.put("conn", connmax + "/" + connmin + "/" + connavg);
                info.put("evict", evictmax + "/" + evictmin + "/" + evictavg);
                info.put("alarm", alarm);

                data.add(info);
            }
        }
        return data;
    }

    public Map<String, Map<String, Object>> getCurrentServerStatsData() {
        Map<String, List<MemcacheStats>> serverStats = getAllServerStats();
        Map<String, MemcachedStatsData> serverStatsData = convertStats(serverStats);
        Map<String, Map<String, Object>> currentStats = new HashMap<String, Map<String, Object>>();
        for (Map.Entry<String, MemcachedStatsData> item : serverStatsData.entrySet()) {
            Map<String, Object> temp = new HashMap<String, Object>();
            if (item.getValue() == null) {
                continue;
            }
            int length = item.getValue().getLength();
            temp.put("QPS", item.getValue().getHitDatas()[length - 1]);
            temp.put("max_memory", item.getValue().getMax_memory());
            temp.put("used_memory", item.getValue().getBytes()[length - 1]);
            temp.put("curr_conn", item.getValue().getConnDatas()[length - 1]);
            temp.put("evict", item.getValue().getEvictionsDatas()[length - 1]);
            long miss = item.getValue().getGetMissDatas()[length - 1];
            long get = item.getValue().getGetsDatas()[length - 1];
            float hitrate;
            if (get > 0) {
                hitrate = (float) ((double) get / (miss + get));
            } else {
                hitrate = 1.0f;
            }
            temp.put("hitrate", hitrate);
            currentStats.put(item.getKey(), temp);
        }

        return currentStats;
    }

    private Map<String, MemcachedStatsData> convertStats(
            Map<String, List<MemcacheStats>> stats) {

        Map<String, MemcachedStatsData> result = new HashMap<String, MemcachedStatsData>();
        for (Map.Entry<String, List<MemcacheStats>> item : stats.entrySet()) {
            if (item.getValue() != null && item.getValue().size() > 1) {
                result.put(item.getKey(), new MemcachedStatsData(item.getValue()));
            }
//			else{
//				result.put(item.getKey(),null);
//			}
        }
        return result;
    }

    private Map<String, List<MemcacheStats>> getAllServerStats() {
        //get all server in cluster
        List<Server> sc = serverService.findAllMemcachedServers();

        long start = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)) / 1000;
        long end = (System.currentTimeMillis()) / 1000;

        Map<String, List<MemcacheStats>> result = new HashMap<String, List<MemcacheStats>>();
        for (Server server : sc) {
            result.put(server.getAddress(), getMemcacheStats(server.getAddress(), start, end));
        }
        return result;
    }

    private List<MemcacheStats> getMemcacheStats(String address, long start, long end) {
        List<MemcacheStats> result = memcacheStatsService.findByServerWithInterval(address, start, end);
        return result;
    }

    public ServerService getServerService() {
        return serverService;
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    public CacheConfigurationService getCacheConfigurationService() {
        return cacheConfigurationService;
    }

    public void setCacheConfigurationService(CacheConfigurationService cacheConfigurationService) {
        this.cacheConfigurationService = cacheConfigurationService;
    }

    public MemcacheStatsService getMemcacheStatsService() {
        return memcacheStatsService;
    }

    public void setMemcacheStatsService(MemcacheStatsService memcacheStatsService) {
        this.memcacheStatsService = memcacheStatsService;
    }
}
