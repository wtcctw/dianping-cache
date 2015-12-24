package com.dianping.cache.alarm.alarmtemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class AlarmConfigTemplate {

    int id = -1;

    String clusterName;

    Date createTime;

    Date updateTime;

    public int getId() {
        return id;
    }

    public AlarmConfigTemplate setId(int id) {
        this.id = id;
        return this;
    }


    public String getClusterName() {
        return clusterName;
    }

    public AlarmConfigTemplate setClusterName(String clusterName) {
        this.clusterName = clusterName;
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
