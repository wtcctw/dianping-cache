package com.dianping.squirrel.cluster.redis;

import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class CPUInfo extends AbstractInfo {
    private double used_cpu_sys;
    private double used_cpu_user;
    private double used_cpu_sys_children;
    private double used_cpu_user_children;

    public CPUInfo() {
        this.infoSegmentName = "cpu";
    }

    public CPUInfo(Map<String,String> infoMap){
        this.used_cpu_sys = NumberUtils.toDouble(infoMap.get("used_cpu_sys"),0.0);
        this.used_cpu_user = NumberUtils.toDouble(infoMap.get("used_cpu_user"),0.0);
        this.used_cpu_sys_children = NumberUtils.toDouble(infoMap.get("used_cpu_sys_children"),0.0);
        this.used_cpu_user_children = NumberUtils.toDouble(infoMap.get("used_cpu_user_children"),0.0);
    }

    public double getUsed_cpu_sys() {
        return used_cpu_sys;
    }

    public void setUsed_cpu_sys(double used_cpu_sys) {
        this.used_cpu_sys = used_cpu_sys;
    }

    public double getUsed_cpu_user() {
        return used_cpu_user;
    }

    public void setUsed_cpu_user(double used_cpu_user) {
        this.used_cpu_user = used_cpu_user;
    }

    public double getUsed_cpu_sys_children() {
        return used_cpu_sys_children;
    }

    public void setUsed_cpu_sys_children(double used_cpu_sys_children) {
        this.used_cpu_sys_children = used_cpu_sys_children;
    }

    public double getUsed_cpu_user_children() {
        return used_cpu_user_children;
    }

    public void setUsed_cpu_user_children(double used_cpu_user_children) {
        this.used_cpu_user_children = used_cpu_user_children;
    }


    @Override
    public String toString() {
        return "CPUInfo{" +
                "used_cpu_sys=" + used_cpu_sys +
                ", used_cpu_user=" + used_cpu_user +
                ", used_cpu_sys_children=" + used_cpu_sys_children +
                ", used_cpu_user_children=" + used_cpu_user_children +
                '}';
    }
}
