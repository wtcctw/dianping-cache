package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.scale.exceptions.ScaleException;
import com.dianping.cache.service.ReshardRecordService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by dp on 15/12/25.
 */
@Component
public class ReshardPlan {

    private int id;

    private String cluster;

    private int totalSlotsToMigrate;

    private int slotsDone;

    private int slotInMigrate;

    private List<String> srcNode;

    private List<String> desNode;

    private int status;

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
            baseLine = totalSlots / totalNodes + (totalSlots % totalNodes == 0 ? 0 : 1);
        }else{
            totalNodes = desNodeServer.size();
            baseLine = 0;
        }
        avgSlots = totalSlots / totalNodes + (totalSlots % totalNodes == 0 ? 0 : 1);
        int order = 0;
        for(RedisServer desServer : desNodeServer){
            int need = avgSlots - desCapacity.get(desServer.getAddress());
            if(need <= 0){
                continue;
            }
            for(RedisServer srcServer : srcNodeServer){
                int slotToMigrate;
                int srcSlotsNum = srcCapacity.get(srcServer.getAddress());
                if((srcSlotsNum > baseLine)&& need > 0){
                    int spareSlot = srcSlotsNum - baseLine;
                    slotToMigrate = spareSlot - need > 0 ? need : spareSlot;
                    need -= slotToMigrate;
                    srcCapacity.put(srcServer.getAddress(),srcSlotsNum - slotToMigrate);
                    List<Integer> slotsToMigrateList = srcServer.getSlotList().subList(srcServer.getSlotSize()-srcSlotsNum,srcServer.getSlotSize() - (srcSlotsNum-slotToMigrate));
                    String slotsToMigrateString = Slot.slotListToString(slotsToMigrateList);
                    ReshardRecord reshardRecord = new ReshardRecord();
                    reshardRecord.setCluster(cluster);
                    reshardRecord.setSrcNode(srcServer.getAddress());
                    reshardRecord.setDesNode(desServer.getAddress());
                    reshardRecord.setSlotsToMigrate(slotsToMigrateString);
                    reshardRecord.setMigrateSwitch(false);
                    reshardRecord.setOrder(order++);
                    reshardRecords.add(reshardRecord);
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
        RedisCluster redisCluster = RedisManager.refreshCache(cluster);
        if(redisCluster == null){
            throw new ScaleException("can't find cluster " + cluster);
        }
        List<RedisServer> servers = new ArrayList<RedisServer>();
        for(String address : addressList){
            RedisServer server = redisCluster.getServer(address);
            if(server.isSlave()){
                throw new ScaleException("can't migrate slave node");
            }
            servers.add(server);
        }
        return servers;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getSrcNode() {
        return srcNode;
    }

    public List<String> getDesNode() {
        return desNode;
    }
}
