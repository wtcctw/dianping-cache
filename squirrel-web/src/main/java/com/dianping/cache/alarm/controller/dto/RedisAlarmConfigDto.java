package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class RedisAlarmConfigDto {

    private String redisKey;

    private boolean alarmSwitch;

    private int memThreshold;

    private int qpsThreshold;

    private int connThreshold;

    private Date createTime;

    private Date updateTime;

    private boolean isUpdate;

    public RedisAlarmConfigDto() {
    }

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
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

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }
}
