package com.dianping.cache.alarm.entity;

import com.dianping.cache.alarm.alarmtemplate.AlarmConfigTemplate;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class MemcacheTemplate extends AlarmConfigTemplate {

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

    private int connThreshold;

    private int connFluc;

    private int connBase;

    private int connInterval;

    public boolean isDown() {
        return isDown;
    }

    public MemcacheTemplate setIsDown(boolean isDown) {
        this.isDown = isDown;
        return this;
    }

    public boolean isCheckHistory() {
        return checkHistory;
    }

    public MemcacheTemplate setCheckHistory(boolean checkHistory) {
        this.checkHistory = checkHistory;
        return this;
    }

    public int getMemThreshold() {
        return memThreshold;
    }

    public MemcacheTemplate setMemThreshold(int memThreshold) {
        this.memThreshold = memThreshold;
        return this;
    }

    public int getMemFluc() {
        return memFluc;
    }

    public MemcacheTemplate setMemFluc(int memFluc) {
        this.memFluc = memFluc;
        return this;
    }

    public int getMemBase() {
        return memBase;
    }

    public MemcacheTemplate setMemBase(int memBase) {
        this.memBase = memBase;
        return this;
    }

    public int getMemInterval() {
        return memInterval;
    }

    public MemcacheTemplate setMemInterval(int memInterval) {
        this.memInterval = memInterval;
        return this;
    }

    public int getQpsThreshold() {
        return qpsThreshold;
    }

    public MemcacheTemplate setQpsThreshold(int qpsThreshold) {
        this.qpsThreshold = qpsThreshold;
        return this;
    }

    public int getQpsFluc() {
        return qpsFluc;
    }

    public MemcacheTemplate setQpsFluc(int qpsFluc) {
        this.qpsFluc = qpsFluc;
        return this;
    }

    public int getQpsBase() {
        return qpsBase;
    }

    public MemcacheTemplate setQpsBase(int qpsBase) {
        this.qpsBase = qpsBase;
        return this;
    }

    public int getQpsInterval() {
        return qpsInterval;
    }

    public MemcacheTemplate setQpsInterval(int qpsInterval) {
        this.qpsInterval = qpsInterval;
        return this;
    }

    public int getConnThreshold() {
        return connThreshold;
    }

    public MemcacheTemplate setConnThreshold(int connThreshold) {
        this.connThreshold = connThreshold;
        return this;
    }

    public int getConnFluc() {
        return connFluc;
    }

    public MemcacheTemplate setConnFluc(int connFluc) {
        this.connFluc = connFluc;
        return this;
    }

    public int getConnBase() {
        return connBase;
    }

    public MemcacheTemplate setConnBase(int connBase) {
        this.connBase = connBase;
        return this;
    }

    public int getConnInterval() {
        return connInterval;
    }

    public MemcacheTemplate setConnInterval(int connInterval) {
        this.connInterval = connInterval;
        return this;
    }
}
