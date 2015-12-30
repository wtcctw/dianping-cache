package com.dianping.cache.alarm.controller.dto;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/9.
 */
public class AlarmSearchDto extends BaseQueryDto {

    private String receiver;

    private Date startTime;

    private Date endTime;

    public AlarmSearchDto(){

    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
