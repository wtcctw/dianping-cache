package com.dianping.squirrel.monitor.data;

public class Data {

    public enum DataType {MemcachedStats, MemcachedHeartbeat, RedisStats, ZabbixStats, CatStats};
    
    private String type;
    private String server;
    private String cluster;
    private Stats stats;
    
    public Object getType() {
        return null;
    }
    
}
