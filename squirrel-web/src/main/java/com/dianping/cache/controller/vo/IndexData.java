package com.dianping.cache.controller.vo;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/22.
 */
public class IndexData {
    private int totalNum;
    private int countInc;

    private int capacity;
    private String capacityString;
    private int redisCapacity;
    private String redisCapacityString;
    private int memcachedCapacity;
    private String memcachedCapacityString;

    private int dataInc;
    private int redisDataInc;
    private int mecachedDataInc;

    private int totalMachines;
    private int totalMachineCapacity;
    private int freeMachineCapacity;
    private String totalMachineCapacityString;
    private String freeMachineCapacityString;


    private List<String> createTimeList;

    private List<Long> totalCountListSquirrel;

    private List<Double>failurePercentListSquirrel;

    private List<Double>avgDelayListSquirrel;

    private List<Long> totalCountListCache;

    private List<Double>failurePercentListCache;

    private List<Double>avgDelayListCache;


    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getCountInc() {
        return countInc;
    }

    public void setCountInc(int countInc) {
        this.countInc = countInc;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        capacityString = convertT(capacity);
    }

    public int getRedisCapacity() {
        return redisCapacity;
    }

    public void setRedisCapacity(int capacity) {
        this.redisCapacity = capacity;
        redisCapacityString = convertT(capacity);
    }

    public int getMemcachedCapacity() {
        return memcachedCapacity;
    }

    public void setMemcachedCapacity(int memcachedCapacity) {
        this.memcachedCapacity = memcachedCapacity;
        memcachedCapacityString = convertT(memcachedCapacity);
    }


    public int getTotalMachines() {
        return totalMachines;
    }

    public void setTotalMachines(int totalMachines) {
        this.totalMachines = totalMachines;
    }

    public int getTotalMachineCapacity() {
        return totalMachineCapacity;
    }

    public void setTotalMachineCapacity(int totalMachineCapacity) {
        this.totalMachineCapacity = totalMachineCapacity;
        totalMachineCapacityString = convertT(totalMachineCapacity);
    }

    public int getFreeMachineCapacity() {
        return freeMachineCapacity;
    }

    public void setFreeMachineCapacity(int freeMachineCapacity) {
        this.freeMachineCapacity = freeMachineCapacity;
        freeMachineCapacityString = convertT(freeMachineCapacity);
    }

    public int getDataInc() {
        return dataInc;
    }

    public void setDataInc(int dataInc) {
        this.dataInc = dataInc;
    }

    public int getRedisDataInc() {
        return redisDataInc;
    }

    public void setRedisDataInc(int redisDataInc) {
        this.redisDataInc = redisDataInc;
    }

    public int getMecachedDataInc() {
        return mecachedDataInc;
    }

    public void setMecachedDataInc(int mecachedDataInc) {
        this.mecachedDataInc = mecachedDataInc;
    }

    public String getCapacityString() {
        return capacityString;
    }

    public void setCapacityString(String capacityString) {
        this.capacityString = capacityString;
    }

    public String getRedisCapacityString() {
        return redisCapacityString;
    }

    public void setRedisCapacityString(String redisCapacityString) {
        this.redisCapacityString = redisCapacityString;
    }

    public String getMemcachedCapacityString() {
        return memcachedCapacityString;
    }

    public void setMemcachedCapacityString(String memcachedCapacityString) {
        this.memcachedCapacityString = memcachedCapacityString;
    }

    public String getTotalMachineCapacityString() {
        return totalMachineCapacityString;
    }

    public void setTotalMachineCapacityString(String totalMachineCapacityString) {
        this.totalMachineCapacityString = totalMachineCapacityString;
    }

    public String getFreeMachineCapacityString() {
        return freeMachineCapacityString;
    }

    public void setFreeMachineCapacityString(String freeMachineCapacityString) {
        this.freeMachineCapacityString = freeMachineCapacityString;
    }

    public List<String> getCreateTimeList() {
        return createTimeList;
    }

    public void setCreateTimeList(List<String> createTimeList) {
        this.createTimeList = createTimeList;
    }

    public List<Long> getTotalCountListSquirrel() {
        return totalCountListSquirrel;
    }

    public void setTotalCountListSquirrel(List<Long> totalCountListSquirrel) {
        this.totalCountListSquirrel = totalCountListSquirrel;
    }

    public List<Double> getFailurePercentListSquirrel() {
        return failurePercentListSquirrel;
    }

    public void setFailurePercentListSquirrel(List<Double> failurePercentListSquirrel) {
        this.failurePercentListSquirrel = failurePercentListSquirrel;
    }

    public List<Double> getAvgDelayListSquirrel() {
        return avgDelayListSquirrel;
    }

    public void setAvgDelayListSquirrel(List<Double> avgDelayListSquirrel) {
        this.avgDelayListSquirrel = avgDelayListSquirrel;
    }

    public List<Long> getTotalCountListCache() {
        return totalCountListCache;
    }

    public void setTotalCountListCache(List<Long> totalCountListCache) {
        this.totalCountListCache = totalCountListCache;
    }

    public List<Double> getFailurePercentListCache() {
        return failurePercentListCache;
    }

    public void setFailurePercentListCache(List<Double> failurePercentListCache) {
        this.failurePercentListCache = failurePercentListCache;
    }

    public List<Double> getAvgDelayListCache() {
        return avgDelayListCache;
    }

    public void setAvgDelayListCache(List<Double> avgDelayListCache) {
        this.avgDelayListCache = avgDelayListCache;
    }

    private String convertT(int capacity){
        if(capacity > 1024){
            double cap = (double) capacity / 1024 ;
            long tmp = Math.round(cap * 100);
            cap =  tmp / 100.0;
            return cap + "T";
        }else{
            return capacity + "G";
        }
    }
}
