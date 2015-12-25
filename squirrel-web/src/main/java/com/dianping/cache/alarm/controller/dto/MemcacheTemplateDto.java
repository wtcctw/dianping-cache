package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class MemcacheTemplateDto {

    private int id;

    private String templateName;

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private boolean isDown;

    private int memThreshold;

    private int qpsThreshold;

    private int connThreshold;

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

    public boolean isDown() {
        return isDown;
    }

    public void setIsDown(boolean isDown) {
        this.isDown = isDown;
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
