package com.dianping.cache.alarm.entity;

/**
 * Created by lvshiyun on 16/1/12.
 */
public class ScanStatistics {

    private int id;

    private long totalCount;

    private long failureCount;

    private double failurePercent;

    private double avgDelay;

    private String createTime;

    private String updateTime;

    public ScanStatistics(){

    }

    public ScanStatistics(long totalCount, long failureCount, double failurePercent, double avgDelay, String createTime, String updateTime) {
        this.setTotalCount(totalCount)
                .setFailureCount(failureCount)
                .setFailurePercent(failurePercent)
                .setAvgDelay(avgDelay)
                .setCreateTime(createTime)
                .setUpdateTime(updateTime);
    }

    public ScanStatistics(int id, long totalCount, long failureCount, double failurePercent, double avgDelay, String createTime, String updateTime){
        this.setId(id)
                .setTotalCount(totalCount)
                .setFailureCount(failureCount)
                .setFailurePercent(failurePercent)
                .setAvgDelay(avgDelay)
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

    public long getTotalCount() {
        return totalCount;
    }

    public ScanStatistics setTotalCount(long totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public ScanStatistics setFailureCount(long failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    public double getFailurePercent() {
        return failurePercent;
    }

    public ScanStatistics setFailurePercent(double failurePercent) {
        this.failurePercent = failurePercent;
        return this;
    }

    public double getAvgDelay() {
        return avgDelay;
    }

    public ScanStatistics setAvgDelay(double avgDelay) {
        this.avgDelay = avgDelay;
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
