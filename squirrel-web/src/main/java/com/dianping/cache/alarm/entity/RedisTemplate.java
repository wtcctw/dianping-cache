package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class RedisTemplate extends AlarmConfigTemplate {

    private boolean isDown;

    private boolean checkHistory;

    private int memThreshold;

    private int memFluc;

    private int memBase;

    private int memInterval;

    private int qpsThreshold;

    private int qpsFluc;

    private int qpsBase;

    private int qpsInterval;

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

    public int getMemFluc() {
        return memFluc;
    }

    public RedisTemplate setMemFluc(int memFluc) {
        this.memFluc = memFluc;
        return this;
    }

    public int getMemBase() {
        return memBase;
    }

    public RedisTemplate setMemBase(int memBase) {
        this.memBase = memBase;
        return this;
    }

    public int getMemInterval() {
        return memInterval;
    }

    public RedisTemplate setMemInterval(int memInterval) {
        this.memInterval = memInterval;
        return this;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public RedisTemplate setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
        return this;
    }

    public int getQpsFluc() {
        return qpsFluc;
    }

    public RedisTemplate setQpsFluc(int qpsFluc) {
        this.qpsFluc = qpsFluc;
        return this;
    }

    public int getQpsBase() {
        return qpsBase;
    }

    public RedisTemplate setQpsBase(int qpsBase) {
        this.qpsBase = qpsBase;
        return this;
    }

    public int getQpsInterval() {
        return qpsInterval;
    }

    public RedisTemplate setQpsInterval(int qpsInterval) {
        this.qpsInterval = qpsInterval;
        return this;
    }
}
