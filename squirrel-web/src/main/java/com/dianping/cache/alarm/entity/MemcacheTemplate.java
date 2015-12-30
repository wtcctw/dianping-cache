package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class MemcacheTemplate extends AlarmConfigTemplate {

    private boolean isDown;

    private int memThreshold;

    private int qpsThreshold;

    private int connThreshold;

    public boolean isDown() {
        return isDown;
    }

    public MemcacheTemplate setIsDown(boolean isDown) {
        this.isDown = isDown;
        return this;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public MemcacheTemplate setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
        return this;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public MemcacheTemplate setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
        return this;
    }

    public int getConnThreshold() {
        return connThreshold;
    }

    public MemcacheTemplate setConnThreshold(int connThreshold) {
        this.connThreshold = connThreshold;
        return this;
    }

}
