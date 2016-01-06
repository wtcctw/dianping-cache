package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class RedisTemplate extends AlarmConfigTemplate {

    private boolean isDown;

    private boolean checkHistory;

    private int memThreshold;

    private int qpsThreshold;

    public boolean isDown() {
        return isDown;
    }

    public RedisTemplate setIsDown(boolean isDown) {
        this.isDown = isDown;
        return this;
    }

    public boolean isCheckHistory() {
        return checkHistory;
    }

    public RedisTemplate setCheckHistory(boolean checkHistory) {
        this.checkHistory = checkHistory;
        return this;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public RedisTemplate setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
        return this;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public RedisTemplate setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
        return this;
    }
}
