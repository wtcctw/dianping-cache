package com.dianping.squirrel.monitor.data;

public class Data {

    public enum DataType {MemcachedStats, MemcachedHeartbeat, RedisStats, ZabbixStats, CatStats}
    
    private DataType type;
    private String server;
    private String cluster;
    private Stats stats;
    
    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
