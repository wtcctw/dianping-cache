package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.scale.exceptions.ScaleException;
import com.dianping.cache.service.ReshardRecordService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dp on 15/12/25.
 */
@Component
public class ReshardPlan {

    //@Autowired
    private ReshardRecordService reshardRecordService;

    private String cluster;

    private List<String> srcNode;

    private List<String> desNode;

    private boolean isAverage;

    private List<ReshardRecord> reshardRecordList;

    public ReshardPlan(){

    }

    public ReshardPlan(String cluster,List<String> srcNode,List<String> desNode,boolean isAverage){
        this.cluster = cluster;
        this.srcNode = srcNode;
        this.desNode = desNode;
        this.isAverage = isAverage;
        reshardRecordList = makePlan();
    }

    public List<ReshardRecord> makePlan(){
        List<ReshardRecord> reshardRecords = new ArrayList<ReshardRecord>();
        List<RedisServer> srcNodeServer = getServerInClusterCache(srcNode);
        List<RedisServer> desNodeServer = getServerInClusterCache(desNode);
        Map<String,Integer> srcCapacity = new HashMap<String, Integer>(srcNodeServer.size());
        Map<String,Integer> desCapacity = new HashMap<String, Integer>(desNodeServer.size());
        int totalNodes;
        int totalSlots,totalSrcSlots = 0,totalDesSlots = 0;
        int avgSlots,baseLine;
        for(RedisServer redisServer : srcNodeServer){
            totalSrcSlots += redisServer.getSlotSize();
            srcCapacity.put(redisServer.getAddress(),redisServer.getSlotSize());
        }
        for(RedisServer redisServer : desNodeServer){
            totalDesSlots += redisServer.getSlotSize();
            desCapacity.put(redisServer.getAddress(),redisServer.getSlotSize());
        }
        totalSlots = totalSrcSlots + totalDesSlots;
        if(isAverage){
            totalNodes = srcNodeServer.size() + desNodeServer.size();
            baseLine = totalSlots / totalNodes + 1;
        }else{
            totalNodes = desNodeServer.size();
            baseLine = 0;
        }
        avgSlots = totalSlots / totalNodes + 1;
        int order = 0;
        for(RedisServer desServer : desNodeServer){
            int need = avgSlots - desCapacity.get(desServer.getAddress());
            if(need < 0){
                continue;
            }
            for(Map.Entry<String,Integer> srcServer : srcCapacity.entrySet()){
                int slotToMigrate = 0;
                int srcSlotsNum = srcServer.getValue();
                if((srcSlotsNum > baseLine)&& need > 0){
                    int spareSlot = srcSlotsNum - baseLine;
                    slotToMigrate = spareSlot - need > 0 ? need : spareSlot;
                    need -= slotToMigrate;
                    srcServer.setValue(srcSlotsNum - slotToMigrate);
                    ReshardRecord reshardRecord = new ReshardRecord();
                    reshardRecord.setCluster(cluster);
                    reshardRecord.setSrcNode(srcServer.getKey());
                    reshardRecord.setDesNode(desServer.getAddress());
                    reshardRecord.setSlotsToMigrate(slotToMigrate);
                    reshardRecord.setMigrateSwitch(false);
                    reshardRecord.setOrder(order++);
                    reshardRecords.add(reshardRecord);
                    //reshardRecordService.insert(reshardRecord);
                }
                if(need <= 0){
                    break;
                }
            }
        }
        return reshardRecords;
    }

    private List<RedisServer> getServerInClusterCache(List<String> addressList){
        if(cluster == null)
            return null;
        RedisCluster redisCluster = RedisManager.getRedisCluster(cluster);
        List<RedisServer> servers = new ArrayList<RedisServer>();
        for(String address : addressList){
            RedisServer server = redisCluster.getServer(address);
            if(server.isSlave()){
                throw new ScaleException("can't migrate slave node");//can't migrate slave node
            }
            servers.add(server);
        }
        return servers;
    }
    public void updateRecode(){

    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public List<ReshardRecord> getReshardRecordList() {
        return reshardRecordList;
    }

    public void setReshardRecordList(List<ReshardRecord> reshardRecordList) {
        this.reshardRecordList = reshardRecordList;
    }

    public static void main(String[] args) {
         List<String> srcNodes = new ArrayList<String>(){{
            add("127.0.0.1:7000");
            add("127.0.0.1:7001");
            add("127.0.0.1:7002");
        }};
         List<String> desNodes = new ArrayList<String>(){{
            add("127.0.0.1:7003");
            add("127.0.0.1:7004");
        }};
        RedisCluster redisCluster = new RedisCluster(srcNodes);
        RedisManager.getClusterCache().put("redis-test", redisCluster);
        ReshardPlan reshardPlan = new ReshardPlan("redis-test", srcNodes, desNodes, false);
        List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
        for (ReshardRecord reshardRecord : reshardRecordList) {
            System.out.println("From :" + reshardRecord.getSrcNode() + "-- > To :" + reshardRecord.getDesNode() + " slots : " + reshardRecord.getSlotsToMigrate());
        }
    }

}
