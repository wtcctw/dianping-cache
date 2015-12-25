package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class AlarmConfigDto {

    private int id;

    private String clusterType;

    private String clusterName;

    private String alarmTemplate;

    private String receiver;

    private boolean toBusiness;

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

    public String getAlarmTemplate() {
        return alarmTemplate;
    }

    public void setAlarmTemplate(String alarmTemplate) {
        this.alarmTemplate = alarmTemplate;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean isToBusiness() {
        return toBusiness;
    }

    public void setToBusiness(boolean toBusiness) {
        this.toBusiness = toBusiness;
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
