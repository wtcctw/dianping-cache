package com.dianping.cache.controller.vo;

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

    private String convertT(int capacity){
        if(capacity > 1024){
            capacity /= 1024;
            double cap = (double) capacity / 1024 ;
            long tmp = Math.round(cap * 100);
            cap =  tmp / 100.0;
            return cap + "T";
        }else{
            return capacity + "G";
        }
    }
}
