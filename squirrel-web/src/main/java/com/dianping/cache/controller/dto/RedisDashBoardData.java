package com.dianping.cache.controller.dto;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.scale.cluster.redis.RedisCluster;
import com.dianping.cache.scale.cluster.redis.RedisInfo;
import com.dianping.cache.scale.cluster.redis.RedisNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp on 16/1/1.
 */
public class RedisDashBoardData extends DashBoardData{

    List<SimpleAnalysisData> datas;
    public RedisDashBoardData(){
        super();
    }
    public RedisDashBoardData(List<RedisCluster> clusters){
        datas = new ArrayList<SimpleAnalysisData>();
        int dangerNum = 0;
        int totalNum = clusters.size();
        for(RedisCluster redisCluster : clusters){
            SimpleAnalysisData simpleAnalysisData = new SimpleAnalysisData(redisCluster);
            if(simpleAnalysisData.analysis()){
                dangerNum++;
                datas.add(0,simpleAnalysisData);
            }else{
                datas.add(simpleAnalysisData);
            }
        }
        int healthyNum = totalNum - dangerNum;
        this.setTotalNum(totalNum);
        this.setDangerNum(dangerNum);
        this.setHealthyNum(healthyNum);
    }

    public void setDatas(List<SimpleAnalysisData> datas) {
        this.datas = datas;
    }

    public List<SimpleAnalysisData> getDatas() {
        return datas;
    }

    public class SimpleAnalysisData{
        int qps;
        long maxMemory;
        float memoryUsage;
        int masterNum;
        int slaveNum;
        boolean QPSAlarm;
        boolean usageAlarm;
        boolean msAlarm;
        boolean clusterAlarm;
        String clusterName;
        RedisCluster redisCluster;
        public SimpleAnalysisData(RedisCluster redisCluster){
            this.redisCluster = redisCluster;
            this.clusterName = redisCluster.getClusterName();
        }
        public boolean analysis(){
            qps = 0;
            long maxMemory_ = 0,usedMemory = 0;
            for(RedisNode node : redisCluster.getNodes()){
                RedisInfo info = node.getMaster().getInfo();
                if(info == null){
                    info = node.getMaster().loadRedisInfo();
                    if(info == null){
                        continue;
                    }
                }
                qps += info.getQps();
                maxMemory_ += info.getMaxMemory();
                usedMemory += info.getUsedMemory();
                masterNum ++;
                if(node.getSlave() != null){
                    slaveNum ++;
                }
            }
            maxMemory = maxMemory_ / 1024;
            memoryUsage = (float) usedMemory / maxMemory_ ;
            int tmp = Math.round(memoryUsage * 10000);
            memoryUsage =  (float) (tmp / 100.0);
            if(qps > 100000){
                QPSAlarm = true;
            }
            if(memoryUsage > 70.0f || maxMemory == 0){
                usageAlarm = true;
            }
            if(masterNum != slaveNum  || masterNum == 0){
                msAlarm = true;
            }
            if(QPSAlarm || usageAlarm || msAlarm){
                clusterAlarm = true;
            }
            return clusterAlarm;
        }

        public int getQps() {
            return qps;
        }

        public void setQps(int qps) {
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

        public int getMasterNum() {
            return masterNum;
        }

        public void setMasterNum(int masterNum) {
            this.masterNum = masterNum;
        }

        public int getSlaveNum() {
            return slaveNum;
        }

        public void setSlaveNum(int slaveNum) {
            this.slaveNum = slaveNum;
        }

        public boolean isQPSAlarm() {
            return QPSAlarm;
        }

        public void setQPSAlarm(boolean QPSAlarm) {
            this.QPSAlarm = QPSAlarm;
        }

        public boolean isUsageAlarm() {
            return usageAlarm;
        }

        public void setUsageAlarm(boolean usageAlarm) {
            this.usageAlarm = usageAlarm;
        }

        public boolean isMsAlarm() {
            return msAlarm;
        }

        public void setMsAlarm(boolean msAlarm) {
            this.msAlarm = msAlarm;
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

        public RedisCluster getRedisCluster() {
            return redisCluster;
        }

        public void setRedisCluster(RedisCluster redisCluster) {
            this.redisCluster = redisCluster;
        }
    }
}


