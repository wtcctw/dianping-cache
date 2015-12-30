package com.dianping.cache.alarm.alarmtemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class AlarmConfigTemplate {

    int id = -1;

    String templateName;

    boolean mailMode;

    boolean smsMode;

    boolean weixinMode;

    Date createTime;

    Date updateTime;

    public int getId() {
        return id;
    }

    public AlarmConfigTemplate setId(int id) {
        this.id = id;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public AlarmConfigTemplate setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public boolean isMailMode() {
        return mailMode;
    }

    public AlarmConfigTemplate setMailMode(boolean mailMode) {
        this.mailMode = mailMode;
        return this;
    }

    public boolean isSmsMode() {
        return smsMode;
    }

    public AlarmConfigTemplate setSmsMode(boolean smsMode) {
        this.smsMode = smsMode;
        return this;
    }

    public boolean isWeixinMode() {
        return weixinMode;
    }

    public AlarmConfigTemplate setWeixinMode(boolean weixinMode) {
        this.weixinMode = weixinMode;
        return this;
    }
    
    public Date getCreateTime() {
        return createTime;
    }

    public AlarmConfigTemplate setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public AlarmConfigTemplate setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
