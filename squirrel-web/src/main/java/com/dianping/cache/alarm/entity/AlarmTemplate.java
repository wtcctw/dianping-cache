package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class AlarmTemplate extends AlarmConfigTemplate {

    private String alarmType;

    private boolean alarmSwitch;

    private int threshold;

    private boolean flucSwitch;

    private int fluc;

    private int base;

    private int alarmInterval;

    public AlarmTemplate(){

    }

    public AlarmTemplate(int id, String templateName, String alarmType, boolean alarmSwitch, int threshold, boolean flucSwitch, int fluc, int base, int alarmInterval,
                            boolean mailMode, boolean smsMode, boolean weixinMode, Date createTime, Date updateTime){

        this.setId(id);
        this.setTemplateName(templateName);
        this.alarmType = alarmType;
        this.alarmSwitch = alarmSwitch;
        this.threshold = threshold;
        this.flucSwitch = flucSwitch;
        this.fluc = fluc;
        this.base = base;
        this.alarmInterval = alarmInterval;
        this.setMailMode(mailMode);
        this.setSmsMode(smsMode);
        this.setWeixinMode(weixinMode);
        this.setCreateTime(createTime);
        this.setUpdateTime(updateTime);

    }


    public String getAlarmType() {
        return alarmType;
    }

    public AlarmTemplate setAlarmType(String alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public boolean isAlarmSwitch() {
        return alarmSwitch;
    }

    public AlarmTemplate setAlarmSwitch(boolean alarmSwitch) {
        this.alarmSwitch = alarmSwitch;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public AlarmTemplate setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public boolean isFlucSwitch() {
        return flucSwitch;
    }

    public AlarmTemplate setFlucSwitch(boolean flucSwitch) {
        this.flucSwitch = flucSwitch;
        return this;
    }

    public int getFluc() {
        return fluc;
    }

    public AlarmTemplate setFluc(int fluc) {
        this.fluc = fluc;
        return this;
    }

    public int getBase() {
        return base;
    }

    public AlarmTemplate setBase(int base) {
        this.base = base;
        return this;
    }

    public int getAlarmInterval() {
        return alarmInterval;
    }

    public AlarmTemplate setAlarmInterval(int alarmInterval) {
        this.alarmInterval = alarmInterval;
        return this;
    }
}
