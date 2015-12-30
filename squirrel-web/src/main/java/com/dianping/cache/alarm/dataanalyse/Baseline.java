package com.dianping.cache.alarm.dataanalyse;

import java.util.Date;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/30.
 */
public class Baseline {

    String clusterType;

    String clusterName;

    String ip_port;

    String category;

    Map<Date,Integer> baseValue;

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getIp_port() {
        return ip_port;
    }

    public void setIp_port(String ip_port) {
        this.ip_port = ip_port;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getBaseValue(Date date) {
        return baseValue.get(date);
    }

    public void setBaseValue(Date date, Integer value) {
        this.baseValue.put(date, value);
    }
}
