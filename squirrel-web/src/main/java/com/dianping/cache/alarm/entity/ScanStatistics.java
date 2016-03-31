package com.dianping.cache.alarm.entity;

/**
 * Created by lvshiyun on 16/1/12.
 */
public class ScanStatistics {

    private int id;

    private long totalCountSquirrel;

    private long failureCountSquirrel;

    private double failurePercentSquirrel;

    private double avgDelaySquirrel;

    private long totalCountCache;

    private long failureCountCache;

    private double failurePercentCache;

    private double avgDelayCache;

    private String createTime;

    private String updateTime;

    public ScanStatistics(){

    }

    public ScanStatistics(long totalCountSquirrel, long failureCountSquirrel, double failurePercentSquirrel, double avgDelaySquirrel,long totalCountCache, long failureCountCache, double failurePercentCache, double avgDelayCache, String createTime, String updateTime) {
        this.setTotalCountSquirrel(totalCountSquirrel)
                .setTotalCountCache(totalCountCache)
                .setFailureCountSquirrel(failureCountSquirrel)
                .setFailureCountCache(failureCountCache)
                .setFailurePercentSquirrel(failurePercentSquirrel)
                .setFailureCountCache(failureCountCache)
                .setAvgDelaySquirrel(avgDelaySquirrel)
                .setAvgDelayCache(avgDelayCache)
                .setCreateTime(createTime)
                .setUpdateTime(updateTime);
    }


    public int getId() {
        return id;
    }

    public ScanStatistics setId(int id) {
        this.id = id;
        return this;
    }

    public long getTotalCountSquirrel() {
        return totalCountSquirrel;
    }

    public ScanStatistics setTotalCountSquirrel(long totalCountSquirrel) {
        this.totalCountSquirrel = totalCountSquirrel;
        return this;
    }

    public long getFailureCountSquirrel() {
        return failureCountSquirrel;
    }

    public ScanStatistics setFailureCountSquirrel(long failureCountSquirrel) {
        this.failureCountSquirrel = failureCountSquirrel;
        return this;
    }

    public double getFailurePercentSquirrel() {
        return failurePercentSquirrel;
    }

    public ScanStatistics setFailurePercentSquirrel(double failurePercentSquirrel) {
        this.failurePercentSquirrel = failurePercentSquirrel;
        return this;
    }

    public double getAvgDelaySquirrel() {
        return avgDelaySquirrel;
    }

    public ScanStatistics setAvgDelaySquirrel(double avgDelaySquirrel) {
        this.avgDelaySquirrel = avgDelaySquirrel;
        return this;
    }

    public long getTotalCountCache() {
        return totalCountCache;
    }

    public ScanStatistics setTotalCountCache(long totalCountCache) {
        this.totalCountCache = totalCountCache;
        return this;
    }

    public long getFailureCountCache() {
        return failureCountCache;
    }

    public ScanStatistics setFailureCountCache(long failureCountCache) {
        this.failureCountCache = failureCountCache;
        return this;
    }

    public double getFailurePercentCache() {
        return failurePercentCache;
    }

    public ScanStatistics setFailurePercentCache(double failurePercentCache) {
        this.failurePercentCache = failurePercentCache;
        return this;
    }

    public double getAvgDelayCache() {
        return avgDelayCache;
    }

    public ScanStatistics setAvgDelayCache(double avgDelayCache) {
        this.avgDelayCache = avgDelayCache;
        return this;
    }

    public String getCreateTime() {
        return createTime;
    }

    public ScanStatistics setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public ScanStatistics setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
