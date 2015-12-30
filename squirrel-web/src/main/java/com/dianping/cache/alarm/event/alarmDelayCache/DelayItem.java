package com.dianping.cache.alarm.event.alarmDelayCache;

import com.dianping.cache.alarm.entity.AlarmDetail;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/26.
 */
public class DelayItem {
    String detail;

    int count;

    Date timeStamp;

    public DelayItem(AlarmDetail alarmDetail){
        this.detail = alarmDetail.getAlarmDetail();
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
