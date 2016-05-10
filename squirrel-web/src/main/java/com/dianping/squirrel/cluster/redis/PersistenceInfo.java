package com.dianping.squirrel.cluster.redis;

import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class PersistenceInfo extends AbstractInfo {

    private int loading;
    private long rdb_changes_since_last_save;
    private int rdb_bgsave_in_progress;
    private long rdb_last_save_time;
    private String rdb_last_bgsave_status;
    private long rdb_last_bgsave_time_sec;
    private long rdb_current_bgsave_time_sec;
    private int aof_enabled;
    private int aof_rewrite_in_progress;
    private int aof_rewrite_scheduled;
    private long aof_last_rewrite_time_sec;
    private long aof_current_rewrite_time_sec;
    private String aof_last_bgrewrite_status;
    private String aof_last_write_status;
    private long aof_current_size;
    private long aof_base_size;
    private long aof_pending_rewrite;
    private long aof_buffer_length;
    private long aof_rewrite_buffer_length;
    private int aof_pending_bio_fsync;
    private int aof_delayed_fsync;

    public PersistenceInfo() {
        this.infoSegmentName = "persistence";
    }

    public PersistenceInfo(Map<String,String> infoMap) {
        super(infoMap);
    }

    public PersistenceInfo(String info) {
        super(info);
    }

    @Override
    public void init() {
        loading = NumberUtils.toInt(infoMap.get("loading"),0);
        rdb_changes_since_last_save = NumberUtils.toLong(infoMap.get("rdb_changes_since_last_save"),0);
        rdb_bgsave_in_progress = NumberUtils.toInt(infoMap.get("rdb_bgsave_in_progress"),0);
        rdb_last_save_time = NumberUtils.toLong(infoMap.get("rdb_last_save_time"),0);
        rdb_last_bgsave_status = infoMap.get("rdb_last_bgsave_status");
        rdb_last_bgsave_time_sec = NumberUtils.toLong(infoMap.get("rdb_last_bgsave_time_sec"),0);
        rdb_current_bgsave_time_sec = NumberUtils.toLong(infoMap.get("rdb_current_bgsave_time_sec"),0);
        aof_enabled = NumberUtils.toInt(infoMap.get("aof_enabled"),0);
        aof_rewrite_in_progress = NumberUtils.toInt(infoMap.get("aof_rewrite_in_progress"),0);
        aof_rewrite_scheduled = NumberUtils.toInt(infoMap.get("aof_rewrite_scheduled"),0);
        aof_last_rewrite_time_sec = NumberUtils.toLong(infoMap.get("aof_last_rewrite_time_sec"),0);
        aof_current_rewrite_time_sec = NumberUtils.toLong(infoMap.get("aof_current_rewrite_time_sec"),0);
        aof_last_bgrewrite_status = infoMap.get("aof_last_bgrewrite_status");
        aof_last_write_status = infoMap.get("aof_last_write_status");
        aof_current_size = NumberUtils.toLong(infoMap.get("aof_current_size"),0);
        aof_base_size = NumberUtils.toLong(infoMap.get("aof_base_size"),0);
        aof_pending_rewrite = NumberUtils.toLong(infoMap.get("aof_pending_rewrite"),0);
        aof_buffer_length = NumberUtils.toLong(infoMap.get("aof_buffer_length"),0);
        aof_rewrite_buffer_length = NumberUtils.toLong(infoMap.get("aof_rewrite_buffer_length"),0);
        aof_pending_bio_fsync = NumberUtils.toInt(infoMap.get("aof_pending_bio_fsync"),0);
        aof_delayed_fsync = NumberUtils.toInt(infoMap.get("aof_delayed_fsync"),0);
    }

    public int getLoading() {
        return loading;
    }

    public long getRdb_changes_since_last_save() {
        return rdb_changes_since_last_save;
    }

    public int getRdb_bgsave_in_progress() {
        return rdb_bgsave_in_progress;
    }

    public long getRdb_last_save_time() {
        return rdb_last_save_time;
    }

    public String getRdb_last_bgsave_status() {
        return rdb_last_bgsave_status;
    }

    public long getRdb_last_bgsave_time_sec() {
        return rdb_last_bgsave_time_sec;
    }

    public long getRdb_current_bgsave_time_sec() {
        return rdb_current_bgsave_time_sec;
    }

    public int getAof_enabled() {
        return aof_enabled;
    }

    public int getAof_rewrite_in_progress() {
        return aof_rewrite_in_progress;
    }

    public int getAof_rewrite_scheduled() {
        return aof_rewrite_scheduled;
    }

    public long getAof_last_rewrite_time_sec() {
        return aof_last_rewrite_time_sec;
    }

    public long getAof_current_rewrite_time_sec() {
        return aof_current_rewrite_time_sec;
    }

    public String getAof_last_bgrewrite_status() {
        return aof_last_bgrewrite_status;
    }

    public String getAof_last_write_status() {
        return aof_last_write_status;
    }

    public long getAof_current_size() {
        return aof_current_size;
    }

    public long getAof_base_size() {
        return aof_base_size;
    }

    public long getAof_pending_rewrite() {
        return aof_pending_rewrite;
    }

    public long getAof_buffer_length() {
        return aof_buffer_length;
    }

    public long getAof_rewrite_buffer_length() {
        return aof_rewrite_buffer_length;
    }

    public int getAof_pending_bio_fsync() {
        return aof_pending_bio_fsync;
    }

    public int getAof_delayed_fsync() {
        return aof_delayed_fsync;
    }

    @Override
    public String toString() {
        return "PersistenceInfo{" +
                "loading=" + loading +
                ", rdb_changes_since_last_save=" + rdb_changes_since_last_save +
                ", rdb_bgsave_in_progress=" + rdb_bgsave_in_progress +
                ", rdb_last_save_time=" + rdb_last_save_time +
                ", rdb_last_bgsave_status='" + rdb_last_bgsave_status + '\'' +
                ", rdb_last_bgsave_time_sec=" + rdb_last_bgsave_time_sec +
                ", rdb_current_bgsave_time_sec=" + rdb_current_bgsave_time_sec +
                ", aof_enabled=" + aof_enabled +
                ", aof_rewrite_in_progress=" + aof_rewrite_in_progress +
                ", aof_rewrite_scheduled=" + aof_rewrite_scheduled +
                ", aof_last_rewrite_time_sec=" + aof_last_rewrite_time_sec +
                ", aof_current_rewrite_time_sec=" + aof_current_rewrite_time_sec +
                ", aof_last_bgrewrite_status='" + aof_last_bgrewrite_status + '\'' +
                ", aof_last_write_status='" + aof_last_write_status + '\'' +
                ", aof_current_size=" + aof_current_size +
                ", aof_base_size=" + aof_base_size +
                ", aof_pending_rewrite=" + aof_pending_rewrite +
                ", aof_buffer_length=" + aof_buffer_length +
                ", aof_rewrite_buffer_length=" + aof_rewrite_buffer_length +
                ", aof_pending_bio_fsync=" + aof_pending_bio_fsync +
                ", aof_delayed_fsync=" + aof_delayed_fsync +
                '}';
    }
}
