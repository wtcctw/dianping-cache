package com.dianping.cache.alarm.entity;

/**
 * Created by lvshiyun on 16/1/12.
 */
public class ScanDetail {

    private int id;

    private String cacheName;

    private String project;

    private int totalCount;

    private int failCount;

    private double failPercent;

    private double minValue;

    private double maxValue;

    private double avgValue;

    private double sumValue;

    private double sum2;

    private double std;

    private double tps;

    private double line95Value;

    private double line99Value;

    private String createTime;

    private String updateTime;

    public int getId() {
        return id;
    }

    public ScanDetail setId(int id) {
        this.id = id;
        return this;
    }

    public double getMinValue() {
        return minValue;
    }

    public ScanDetail setMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public ScanDetail setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public double getAvgValue() {
        return avgValue;
    }

    public ScanDetail setAvgValue(double avgValue) {
        this.avgValue = avgValue;
        return this;
    }
    
    public double getSumValue() {
        return sumValue;
    }

    public ScanDetail setSumValue(double sumValue) {
        this.sumValue = sumValue;
        return this;
    }

    public String getCacheName() {
        return cacheName;
    }

    public ScanDetail setCacheName(String cacheName) {
        this.cacheName = cacheName;
        return this;
    }

    public String getProject() {
        return project;
    }

    public ScanDetail setProject(String project) {
        this.project = project;
        return this;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public ScanDetail setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public int getFailCount() {
        return failCount;
    }

    public ScanDetail setFailCount(int failCount) {
        this.failCount = failCount;
        return this;
    }

    public double getFailPercent() {
        return failPercent;
    }

    public ScanDetail setFailPercent(double failPercent) {
        this.failPercent = failPercent;
        return this;
    }


    public double getSum2() {
        return sum2;
    }

    public ScanDetail setSum2(double sum2) {
        this.sum2 = sum2;
        return this;
    }

    public double getStd() {
        return std;
    }

    public ScanDetail setStd(double std) {
        this.std = std;
        return this;
    }

    public double getTps() {
        return tps;
    }

    public ScanDetail setTps(double tps) {
        this.tps = tps;
        return this;
    }

    public double getLine95Value() {
        return line95Value;
    }

    public ScanDetail setLine95Value(double line95Value) {
        this.line95Value = line95Value;
        return this;
    }

    public double getLine99Value() {
        return line99Value;
    }

    public ScanDetail setLine99Value(double line99Value) {
        this.line99Value = line99Value;
        return this;
    }

    public String getCreateTime() {
        return createTime;
    }

    public ScanDetail setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public ScanDetail setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
