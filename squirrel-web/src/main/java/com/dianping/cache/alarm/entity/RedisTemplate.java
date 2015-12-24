package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class RedisTemplate extends AlarmConfigTemplate {

    private boolean isDown;

    private int memThreshold;

    public boolean isDown() {
        return isDown;
    }

    public RedisTemplate setIsDown(boolean isDown) {
        this.isDown = isDown;
        return this;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public RedisTemplate setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
        return this;
    }
}
