package com.dianping.cache.controller.vo;

import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.cluster.redis.RedisCluster;
import com.dianping.cache.scale.cluster.redis.RedisInfo;
import com.dianping.cache.scale.cluster.redis.RedisNode;
import com.dianping.cache.scale.cluster.redis.RedisServer;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.util.SpringLocator;

import java.util.*;

/**
 * Created by dp on 16/1/1.
 */
public class RedisDashBoardData extends DashBoardData{

    List<SimpleAnalysisData> datas;
    public RedisDashBoardData(){
        super();
    }
    public RedisDashBoardData(Collection<RedisCluster> clusters){
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
        int disperse;
        boolean qpsAlarm;
        boolean usageAlarm;
        boolean msAlarm;
        boolean disperseAlarm;
        boolean clusterAlarm;
        String clusterName;
        RedisCluster redisCluster;
        PieSeries[] rate;
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
                qpsAlarm = true;
            }
            if(memoryUsage > 70.0f || maxMemory == 0){
                usageAlarm = true;
            }
            if(masterNum != slaveNum  || masterNum == 0 || redisCluster.getFailedServers().size() > 0){
                msAlarm = true;
            }
            agentRate();
            if(qpsAlarm || usageAlarm || msAlarm || disperseAlarm){
                clusterAlarm = true;
            }

            return clusterAlarm;
        }

        private void   agentRate(){
            disperse = 0;
            ServerService serverService = SpringLocator.getBean("serverService");
            Set<String> allAddress = new HashSet<String>();
            Map<String,Integer> count = new HashMap<String, Integer>();
            for(RedisServer alive : redisCluster.getAllAliveServer()){
                allAddress.add(alive.getAddress());
            }
//            for(RedisServer failed : redisCluster.getFailedServers()){
//                allAddress.add(failed.getAddress());
//            }
            Map<String,String> tempAddressList = new HashMap<String, String>();
            for(String address : allAddress){
                Server server = serverService.findByAddress(address);
                String agent = "other";
                if(server != null && server.getHostIp() != null){
                    agent = server.getHostIp();
                }
                Integer number = count.get(agent);
                String addressList = tempAddressList.get(agent);
                count.put(agent,number == null ? 1 : number+1);
                tempAddressList.put(agent,addressList == null ? "<br>" + address : addressList + "<br>" + address);
            }
            int total = allAddress.size();
            PieSeries[] resultRate = new PieSeries[count.size()];

            List<Map.Entry<String,Integer>> sortMapByValue = new ArrayList<Map.Entry<String, Integer>>(count.entrySet());
            Collections.sort(sortMapByValue, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue() - o1.getValue();
                }
            });

            int index = 0;
            for(Map.Entry<String,Integer> entity : sortMapByValue){
                String host = entity.getKey();
                Float value = (float)(entity.getValue().intValue())/total * 100;
                if(value > 25.0F){
                    this.disperse++;
                    disperseAlarm = true;
                }
                resultRate[index++] = new PieSeries(host,value,tempAddressList.get(host));
            }
            rate = resultRate;
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

        public boolean isQpsAlarm() {
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

        public PieSeries[] getRate() {
            return rate;
        }

        public void setRate(PieSeries[] rate) {
            this.rate = rate;
        }

        public int getDisperse() {
            return disperse;
        }

        public void setDisperse(int disperse) {
            this.disperse = disperse;
        }

        public boolean getDisperseAlarm() {
            return disperseAlarm;
        }

        public void setDisperseAlarm(boolean disperseAlarm) {
            this.disperseAlarm = disperseAlarm;
        }
    }

    public class PieSeries {
        String name;
        float y;
        String address;

        public PieSeries() {
        }

        public PieSeries(String name, float y) {
            this.name = name;
            this.y = y;
        }

        public PieSeries(String name, float y, String address) {
            this.name = name;
            this.y = y;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }


    }
}


