package com.dianping.cache.entity;

/**
 * Created by thunder on 16/1/14.
 */
public class CategoryStats {
    int id;
    String category;
    long keyCount;
    long keySize;
    long valueSize;
    long updateTime;
    String hostAndPort;

    public String getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getKeyCount() {
        return keyCount;
    }

    public void setKeyCount(long keyCount) {
        this.keyCount = keyCount;
    }

    public long getKeySize() {
        return keySize;
    }

    public void setKeySize(long keySize) {
        this.keySize = keySize;
    }

    public long getValueSize() {
        return valueSize;
    }

    public void setValueSize(long valueSize) {
        this.valueSize = valueSize;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

}
