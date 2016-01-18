package com.dianping.cache.controller.vo;

import com.dianping.cache.entity.CacheConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dp on 16/1/4.
 */
public class MemcachedDashBoardData  extends DashBoardData {
    List<SimpleAnalysisData> datas = new ArrayList<SimpleAnalysisData>();
    public MemcachedDashBoardData(){
    }
    public List<SimpleAnalysisData> getDatas() {
        return datas;
    }
    public void setDatas(List<SimpleAnalysisData> datas) {
        this.datas = datas;
    }
    public MemcachedDashBoardData(List<CacheConfiguration> configurations, Map<String,Map<String, Object>> statsMap){

        int dangerNum = 0;
        int totalNum = 0;

        for(CacheConfiguration configuration : configurations){
            if(!configuration.getCacheKey().contains("memcached")
                    || "memcached-leo".equals(configuration.getCacheKey())
                    || !"".equals(configuration.getSwimlane())){
                continue;
            }
            totalNum++;
            SimpleAnalysisData data = new SimpleAnalysisData(configuration.getCacheKey());
            data.setConfiguration(configuration);
            List<String> servers = configuration.getServerList();
            int alive = 0;
            long qps = 0,maxmem = 0,memused = 0;
            float hitrate = 0;
            for(String server : servers){
                if(statsMap.get(server) != null){
                    alive++;
                    maxmem += (Long)statsMap.get(server).get("max_memory");
                    memused += (Long)statsMap.get(server).get("used_memory");
                    qps += (Long)statsMap.get(server).get("QPS");
                    hitrate += (Float)statsMap.get(server).get("hitrate");
                }
            }
            data.setServers(servers);
            data.setAlive(alive);
            data.setHitrate(hitrate);
            data.setMaxMemory(maxmem);
            data.setMemUsed(memused);
            data.setQps(qps);
            if(data.analysis()){
                datas.add(0,data);
                dangerNum++;
            }else{
                datas.add(data);
            }
        }

        int healthyNum = totalNum - dangerNum;
        this.setTotalNum(totalNum);
        this.setDangerNum(dangerNum);
        this.setHealthyNum(healthyNum);

    }
    public class SimpleAnalysisData{
        long qps;
        long maxMemory;
        long memUsed;
        float memoryUsage;
        List<String> servers;
        int alive;
        float hitrate;
        boolean qpsAlarm;
        boolean usageAlarm;
        boolean aliveAlarm;
        boolean clusterAlarm;
        String clusterName;
        CacheConfiguration configuration;
        public SimpleAnalysisData(String clusterName){
            this.clusterName = clusterName;
        }
        public boolean analysis(){
            if(servers.size() > alive){
                aliveAlarm = true;
            }
            if(alive > 0){
                qps = qps/alive;
                hitrate = hitrate/alive;
                memoryUsage = (float) memUsed / maxMemory;
                maxMemory /= 1024;
            }else{
                qps = -1;
                maxMemory = 0;
                memUsed = 0;
                memoryUsage = 0;
                clusterAlarm = qpsAlarm = aliveAlarm = usageAlarm=true;
            }
            int tmp = Math.round(memoryUsage * 10000);
            memoryUsage =  (float) (tmp / 100.0);
            tmp = Math.round(hitrate * 10000);
            hitrate = (float) (tmp / 100.0);
            if(qps > 50000){
                qpsAlarm = true;
            }
            if(memoryUsage > 90.0f){
                usageAlarm = true;
            }
            clusterAlarm |= qpsAlarm | aliveAlarm | usageAlarm;
            return clusterAlarm;
        }

        public long getQps() {
            return qps;
        }

        public void setQps(long qps) {
            this.qps = qps;
        }

        public long getMaxMemory() {
            return maxMemory;
        }

        public void setMaxMemory(long maxMemory) {
            this.maxMemory = maxMemory;
        }

        public float getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(float memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        public List<String> getServers() {
            return servers;
        }

        public void setServers(List<String> servers) {
            this.servers = servers;
        }

        public int getAlive() {
            return alive;
        }

        public void setAlive(int alive) {
            this.alive = alive;
        }

        public float getHitrate() {
            return hitrate;
        }

        public void setHitrate(float hitrate) {
            this.hitrate = hitrate;
        }

        public boolean getQpsAlarm() {
            return qpsAlarm;
        }

        public void setQpsAlarm(boolean qpsAlarm) {
            this.qpsAlarm = qpsAlarm;
        }

        public boolean isUsageAlarm() {
            return usageAlarm;
        }

        public void setUsageAlarm(boolean usageAlarm) {
            this.usageAlarm = usageAlarm;
        }

        public boolean isClusterAlarm() {
            return clusterAlarm;
        }

        public void setClusterAlarm(boolean clusterAlarm) {
            this.clusterAlarm = clusterAlarm;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public long getMemUsed() {
            return memUsed;
        }

        public void setMemUsed(long memUsed) {
            this.memUsed = memUsed;
        }

        public CacheConfiguration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(CacheConfiguration configuration) {
            this.configuration = configuration;
        }

        public boolean isAliveAlarm() {
            return aliveAlarm;
        }

        public void setAliveAlarm(boolean aliveAlarm) {
            this.aliveAlarm = aliveAlarm;
        }
    }
}
