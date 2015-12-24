package com.dianping.cache.alarm.entity;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lvshiyun on 15/12/3.
 */

public class AlarmRecord implements Serializable {


    private static final long serialVersionUID = -7175660124648109004L;

    private int id;

    private String receiver;

    private int alarmType;

    private String alarmTitle;

    private String alarmDetail;

    private Date createTime;

    public int getId() {
        return id;
    }

    public AlarmRecord setId(int id) {
        this.id = id;
        return this;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public AlarmRecord setAlarmType(int alarmType) {
        this.alarmType = alarmType;
        return this;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public AlarmRecord setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
        return this;
    }

    public String getAlarmDetail() {
        return alarmDetail;
    }

    public AlarmRecord setAlarmDetail(String alarmDetail) {
        this.alarmDetail = alarmDetail;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public AlarmRecord setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }
}
