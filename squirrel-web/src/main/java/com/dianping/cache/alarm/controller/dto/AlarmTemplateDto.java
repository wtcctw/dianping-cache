package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class AlarmTemplateDto {

    private int id;

    private String templateName;

    private String alarmType;

    private boolean alarmSwitch;

    private int threshold;

    private boolean flucSwitch;

    private int fluc;

    private int base;

    private int alarmInterval;

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private boolean isUpdate;

    private Date createTime;

    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public boolean isAlarmSwitch() {
        return alarmSwitch;
    }

    public void setAlarmSwitch(boolean alarmSwitch) {
        this.alarmSwitch = alarmSwitch;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isFlucSwitch() {
        return flucSwitch;
    }

    public void setFlucSwitch(boolean flucSwitch) {
        this.flucSwitch = flucSwitch;
    }

    public int getFluc() {
        return fluc;
    }

    public void setFluc(int fluc) {
        this.fluc = fluc;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(int alarmInterval) {
        this.alarmInterval = alarmInterval;
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

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(boolean isUpdate) {
        this.isUpdate = isUpdate;
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
