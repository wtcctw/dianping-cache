package com.dianping.cache.alarm.controller.dto;

import com.dianping.cache.alarm.AlarmType;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class AlarmConfigDto {

    private int id;

    private String clusterType;

    private String clusterName;

    private String alarmType;

    private String alarmRule;

    private int threshold;

    private String receiver;

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private Date createTime;

    private Date updateTime;

    private boolean isUpdate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmRule() {
        return alarmRule;
    }

    public void setAlarmRule(String alarmRule) {
        this.alarmRule = alarmRule;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isMailMode() {
        return mailMode;
    }

    public void setMailMode(boolean mailMode) {
        this.mailMode = mailMode;
    }

    public boolean isSmsMode() {
        return smsMode;
    }

    public void setSmsMode(boolean smsMode) {
        this.smsMode = smsMode;
    }

    public boolean isWeixinMode() {
        return weixinMode;
    }

    public void setWeixinMode(boolean weixinMode) {
        this.weixinMode = weixinMode;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
    }
}
