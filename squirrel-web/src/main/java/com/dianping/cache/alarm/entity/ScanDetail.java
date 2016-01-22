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

    private double minVal;

    private double maxVal;

    private double avgVal;

    private double sumVal;

    private double sum2;

    private double std;

    private double tps;

    private double line95Value;

    private double line99Value;

    private String createTime;

    private String updateTime;

    private int rowspan = 0;

    public int getId() {
        return id;
    }

    public ScanDetail setId(int id) {
        this.id = id;
        return this;
    }

    public double getMinVal() {
        return minVal;
    }

    public ScanDetail setMinVal(double minVal) {
        this.minVal = minVal;
        return this;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public ScanDetail setMaxVal(double maxVal) {
        this.maxVal = maxVal;
        return this;
    }

    public double getAvgVal() {
        return avgVal;
    }

    public ScanDetail setAvgVal(double avgVal) {
        this.avgVal = avgVal;
        return this;
    }

    public double getSumVal() {
        return sumVal;
    }

    public ScanDetail setSumVal(double sumVal) {
        this.sumVal = sumVal;
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

    public int getRowspan() {
        return rowspan;
    }

    public ScanDetail setRowspan(int rowspan) {
        this.rowspan = rowspan;
        return this;
    }
}
