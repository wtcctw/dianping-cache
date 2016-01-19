package com.dianping.cache.controller.vo;

import com.dianping.cache.scale.instance.AppId;

/**
 * dp.wang@dianping.com
 * Created by dp on 16/1/15.
 */
public class NewClusterParams {
    private String clusterName;
    private int nodesNumber;
    private int readTimeout;
    private int connTimeout;
    private int maxRedirects;
    private String password;
    private String swimlane;
    private AppId appId;


    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNodesNumber() {
        return nodesNumber;
    }

    public void setNodesNumber(int nodesNumber) {
        this.nodesNumber = nodesNumber;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public AppId getAppId() {
        return appId;
    }

    public void setAppId(AppId appId) {
        this.appId = appId;
    }
}
