package com.dianping.cache.alarm.controller.dto;

import com.dianping.cache.alarm.AlarmType;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class AlarmMetaDto {

    private String receiver;

    private int metaId;

    private AlarmType type;

    private boolean isSmsMode;

    private boolean isWeiXinMode;

    private boolean isMailMode;

    private String alarmTitle;

    private String alarmDetail;

    private Date createTime;

    private Date updateTime;

    private boolean isUpdate;

    public AlarmMetaDto() {
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getMetaId() {
        return metaId;
    }

    public void setMetaId(int metaId) {
        this.metaId = metaId;
    }

    public AlarmType getType() {
        return type;
    }

    public void setType(AlarmType type) {
        this.type = type;
    }

    public boolean isSmsMode() {
        return isSmsMode;
    }

    public void setIsSmsMode(boolean isSmsMode) {
        this.isSmsMode = isSmsMode;
    }

    public boolean isWeiXinMode() {
        return isWeiXinMode;
    }

    public void setIsWeiXinMode(boolean isWeiXinMode) {
        this.isWeiXinMode = isWeiXinMode;
    }

    public boolean isMailMode() {
        return isMailMode;
    }

    public void setIsMailMode(boolean isMailMode) {
        this.isMailMode = isMailMode;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public void setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
    }

    public String getAlarmDetail() {
        return alarmDetail;
    }

    public void setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
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
