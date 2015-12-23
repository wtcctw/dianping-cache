package com.dianping.cache.alarm.entity;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/10.
 */
public class AlarmDetail extends  AlarmConfig {

    public AlarmDetail(){
        this.setMailMode(true)
                .setReceiver("shiyun.lv")
                .setSmsMode(true)
                .setWeixinMode(true)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
    }

    public AlarmDetail(AlarmConfig alarmConfig){
        this.setId(alarmConfig.getId())
                .setClusterType(alarmConfig.getClusterType())
                .setClusterName(alarmConfig.getClusterName())
                .setAlarmType(alarmConfig.getAlarmType())
                .setAlarmRule(alarmConfig.getAlarmRule())
                .setThreshold(alarmConfig.getThreshold())
                .setReceiver(alarmConfig.getReceiver())
                .setMailMode(alarmConfig.isMailMode())
                .setSmsMode(alarmConfig.isSmsMode())
                .setWeixinMode(alarmConfig.isWeixinMode())
                .setToBusiness(alarmConfig.isToBusiness())
                .setCreateTime(alarmConfig.getCreateTime())
                .setUpdateTime(alarmConfig.getUpdateTime());
    }

    private String alarmTitle;

    private String alarmDetail;

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
