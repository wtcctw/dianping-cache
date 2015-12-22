package com.dianping.cache.alarm.entity;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/10.
 */
public class MemcacheAlarmConfig {
    public static final int DEFAULT_MEM_THRESHOLD = 95;
    public static final int DEFAULT_QPS_THRESHOLD = 80000;
    public static final int DEFAULT_CONN_THRESHOLD = 28000;
    public static final boolean DEFAULT_SWITVH = false;

    private int id;

    private String memcacheKey;

    private boolean alarmSwitch;

    private int memThreshold;

    private int qpsThreshold;

    private int connThreshold;

    private Date createTime;

    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemcacheKey() {
        return memcacheKey;
    }

    public void setMemcacheKey(String memcacheKey) {
        this.memcacheKey = memcacheKey;
    }

    public boolean isAlarmSwitch() {
        return alarmSwitch;
    }

    public void setAlarmSwitch(boolean alarmSwitch) {
        this.alarmSwitch = alarmSwitch;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public void setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public void setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
    }

    public int getConnThreshold() {
        return connThreshold;
    }

    public void setConnThreshold(int connThreshold) {
        this.connThreshold = connThreshold;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
