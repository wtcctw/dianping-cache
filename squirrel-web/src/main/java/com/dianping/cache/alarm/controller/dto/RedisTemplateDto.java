package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class RedisTemplateDto {

    private int id;

    private String templateName;

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private boolean isDown;

    private boolean checkHistory;

    private boolean memSwitch;

    private int memThreshold;

    private int memFluc;

    private int memBase;

    private int memInterval;

    private boolean qpsSwitch;

    private int qpsThreshold;

    private int qpsFluc;

    private int qpsBase;

    private int qpsInterval;

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

    public boolean isCheckHistory() {
        return checkHistory;
    }

    public void setCheckHistory(boolean checkHistory) {
        this.checkHistory = checkHistory;
    }

    public boolean isMemSwitch() {
        return memSwitch;
    }

    public void setMemSwitch(boolean memSwitch) {
        this.memSwitch = memSwitch;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public void setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
    }

    public int getMemFluc() {
        return memFluc;
    }

    public void setMemFluc(int memFluc) {
        this.memFluc = memFluc;
    }

    public int getMemBase() {
        return memBase;
    }

    public void setMemBase(int memBase) {
        this.memBase = memBase;
    }

    public int getMemInterval() {
        return memInterval;
    }

    public void setMemInterval(int memInterval) {
        this.memInterval = memInterval;
    }

    public boolean isQpsSwitch() {
        return qpsSwitch;
    }

    public void setQpsSwitch(boolean qpsSwitch) {
        this.qpsSwitch = qpsSwitch;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public void setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
    }

    public int getQpsFluc() {
        return qpsFluc;
    }

    public void setQpsFluc(int qpsFluc) {
        this.qpsFluc = qpsFluc;
    }

    public int getQpsBase() {
        return qpsBase;
    }

    public void setQpsBase(int qpsBase) {
        this.qpsBase = qpsBase;
    }

    public int getQpsInterval() {
        return qpsInterval;
    }

    public void setQpsInterval(int qpsInterval) {
        this.qpsInterval = qpsInterval;
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
