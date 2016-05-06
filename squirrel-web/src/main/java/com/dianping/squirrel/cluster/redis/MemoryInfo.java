package com.dianping.squirrel.cluster.redis;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class MemoryInfo extends AbstractInfo {

    private long used_memory;
    private String used_memory_human;
    private long used_memory_rss;
    private long used_memory_peak;
    private String used_memory_peak_human;
    private long used_memory_lua;
    private float mem_fragmentation_ratio;
    private String mem_allocator;

    public MemoryInfo() {
        this.infoSegmentName = "memory";
    }

    public MemoryInfo(Map<String,String> infoMap) {
        used_memory = Long.parseLong(infoMap.get("used_memory"));
        used_memory_human = infoMap.get("used_memory_human");
        used_memory_rss = Long.parseLong(infoMap.get("used_memory_rss"));
        used_memory_peak = Long.parseLong(infoMap.get("used_memory_peak"));
        used_memory_peak_human = infoMap.get("used_memory_peak_human");
        used_memory_lua = Long.parseLong(infoMap.get("used_memory_lua"));
        mem_fragmentation_ratio = Float.parseFloat(infoMap.get("mem_fragmentation_ratio"));
        mem_allocator = infoMap.get("mem_allocator");
    }

    public long getUsed_memory() {
        return used_memory;
    }

    public String getUsed_memory_human() {
        return used_memory_human;
    }

    public long getUsed_memory_rss() {
        return used_memory_rss;
    }

    public long getUsed_memory_peak() {
        return used_memory_peak;
    }

    public String getUsed_memory_peak_human() {
        return used_memory_peak_human;
    }

    public long getUsed_memory_lua() {
        return used_memory_lua;
    }

    public float getMem_fragmentation_ratio() {
        return mem_fragmentation_ratio;
    }

    public String getMem_allocator() {
        return mem_allocator;
    }

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "used_memory=" + used_memory +
                ", used_memory_human='" + used_memory_human + '\'' +
                ", used_memory_rss=" + used_memory_rss +
                ", used_memory_peak=" + used_memory_peak +
                ", used_memory_peak_human='" + used_memory_peak_human + '\'' +
                ", used_memory_lua=" + used_memory_lua +
                ", mem_fragmentation_ratio=" + mem_fragmentation_ratio +
                ", mem_allocator='" + mem_allocator + '\'' +
                '}';
    }
}
