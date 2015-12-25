package com.dianping.cache.alarm.entity;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/10.
 */
public class AlarmDetail extends AlarmConfig {

    public AlarmDetail() {
        this
                .setReceiver("shiyun.lv")
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
    }

    public AlarmDetail(AlarmConfig alarmConfig) {
        this.setId(alarmConfig.getId())
                .setClusterType(alarmConfig.getClusterType())
                .setClusterName(alarmConfig.getClusterName())
                .setReceiver(alarmConfig.getReceiver())
                .setToBusiness(alarmConfig.isToBusiness())
                .setCreateTime(alarmConfig.getCreateTime())
                .setUpdateTime(alarmConfig.getUpdateTime());
    }

    private boolean mailMode;

    private boolean smsMode;

    private boolean weixinMode;

    private String alarmTitle;

    private String alarmDetail;

    public boolean isMailMode() {
        return mailMode;
    }

    public AlarmDetail setMailMode(boolean mailMode) {
        this.mailMode = mailMode;
        return this;
    }

    public boolean isSmsMode() {
        return smsMode;
    }

    public AlarmDetail setSmsMode(boolean smsMode) {
        this.smsMode = smsMode;
        return this;
    }

    public boolean isWeixinMode() {
        return weixinMode;
    }

    public AlarmDetail setWeixinMode(boolean weixinMode) {
        this.weixinMode = weixinMode;
        return this;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public AlarmDetail setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
        return this;
    }

    public String getAlarmDetail() {
        return alarmDetail;
    }

    public AlarmDetail setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
        return this;
    }
}
