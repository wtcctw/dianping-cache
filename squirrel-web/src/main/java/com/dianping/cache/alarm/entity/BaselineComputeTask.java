package com.dianping.cache.alarm.entity;

import java.util.Date;

/**
 * Created by lvshiyun on 16/1/4.
 */
public class BaselineComputeTask {

    int id;

    Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
