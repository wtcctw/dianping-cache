package com.dianping.squirrel.entity;

import java.util.Date;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class HulkClusterConfig {
    private int id;
    private int clusterName;
    private String appKey;
    private String authToken;
    private int memoryQuota;
    private Date createTime;
    private Date updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClusterName() {
        return clusterName;
    }

    public void setClusterName(int clusterName) {
        this.clusterName = clusterName;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getMemoryQuota() {
        return memoryQuota;
    }

    public void setMemoryQuota(int memoryQuota) {
        this.memoryQuota = memoryQuota;
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
