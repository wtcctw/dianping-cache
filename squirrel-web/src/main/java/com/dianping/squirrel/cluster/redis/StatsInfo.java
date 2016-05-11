package com.dianping.squirrel.cluster.redis;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/3.
 */
public class StatsInfo extends AbstractInfo {
    private long total_connections_received;
    private long total_commands_processed;
    private long instantaneous_ops_per_sec;
    private long total_net_input_bytes;
    private long total_net_output_bytes;
    private long expired_keys;
    private long evicted_keys;
    private long keyspace_hits;
    private long keyspace_misses;
    private long pubsub_channels;
    private long pubsub_patterns;
    private long latest_fork_usec;
    private double instantaneous_input_kbps;
    private double instantaneous_output_kbps;
    private int rejected_connections;
    private int sync_full;
    private int sync_partial_ok;
    private int sync_partial_err;
    private int migrate_cached_sockets;

    public StatsInfo(Map<String,String> infoMap){
       super(infoMap);
    }

    public StatsInfo(String info) {
        super(info);
    }

    @Override
    public void init() {
        total_connections_received = Long.parseLong(infoMap.get("total_connections_received"));
        total_commands_processed = Long.parseLong(infoMap.get("total_commands_processed"));
        instantaneous_ops_per_sec = Long.parseLong(infoMap.get("instantaneous_ops_per_sec"));
        total_net_input_bytes = Long.parseLong(infoMap.get("total_net_input_bytes"));
        total_net_output_bytes = Long.parseLong(infoMap.get("total_net_output_bytes"));
        expired_keys = Long.parseLong(infoMap.get("expired_keys"));
        evicted_keys = Long.parseLong(infoMap.get("evicted_keys"));
        keyspace_hits = Long.parseLong(infoMap.get("keyspace_hits"));
        keyspace_misses = Long.parseLong(infoMap.get("keyspace_misses"));
        pubsub_channels = Long.parseLong(infoMap.get("pubsub_channels"));
        pubsub_patterns = Long.parseLong(infoMap.get("pubsub_patterns"));
        latest_fork_usec = Long.parseLong(infoMap.get("latest_fork_usec"));
        instantaneous_input_kbps = Double.parseDouble(infoMap.get("instantaneous_input_kbps"));
        instantaneous_output_kbps = Double.parseDouble(infoMap.get("instantaneous_output_kbps"));
        rejected_connections = Integer.parseInt(infoMap.get("rejected_connections"));
        sync_full = Integer.parseInt(infoMap.get("sync_full"));
        sync_partial_ok = Integer.parseInt(infoMap.get("sync_partial_ok"));
        sync_partial_err = Integer.parseInt(infoMap.get("sync_partial_err"));
        migrate_cached_sockets = Integer.parseInt(infoMap.get("migrate_cached_sockets"));
    }

    public long getTotal_connections_received() {
        return total_connections_received;
    }

    public long getTotal_commands_processed() {
        return total_commands_processed;
    }

    public long getInstantaneous_ops_per_sec() {
        return instantaneous_ops_per_sec;
    }

    public long getTotal_net_input_bytes() {
        return total_net_input_bytes;
    }

    public long getTotal_net_output_bytes() {
        return total_net_output_bytes;
    }

    public long getExpired_keys() {
        return expired_keys;
    }

    public long getEvicted_keys() {
        return evicted_keys;
    }

    public long getKeyspace_hits() {
        return keyspace_hits;
    }

    public long getKeyspace_misses() {
        return keyspace_misses;
    }

    public long getPubsub_channels() {
        return pubsub_channels;
    }

    public long getPubsub_patterns() {
        return pubsub_patterns;
    }

    public long getLatest_fork_usec() {
        return latest_fork_usec;
    }

    public double getInstantaneous_input_kbps() {
        return instantaneous_input_kbps;
    }

    public double getInstantaneous_output_kbps() {
        return instantaneous_output_kbps;
    }

    public int getRejected_connections() {
        return rejected_connections;
    }

    public int getSync_full() {
        return sync_full;
    }

    public int getSync_partial_ok() {
        return sync_partial_ok;
    }

    public int getSync_partial_err() {
        return sync_partial_err;
    }

    public int getMigrate_cached_sockets() {
        return migrate_cached_sockets;
    }

    @Override
    public String toString() {
        return "StatsInfo{" +
                "total_connections_received=" + total_connections_received +
                ", total_commands_processed=" + total_commands_processed +
                ", instantaneous_ops_per_sec=" + instantaneous_ops_per_sec +
                ", total_net_input_bytes=" + total_net_input_bytes +
                ", total_net_output_bytes=" + total_net_output_bytes +
                ", expired_keys=" + expired_keys +
                ", evicted_keys=" + evicted_keys +
                ", keyspace_hits=" + keyspace_hits +
                ", keyspace_misses=" + keyspace_misses +
                ", pubsub_channels=" + pubsub_channels +
                ", pubsub_patterns=" + pubsub_patterns +
                ", latest_fork_usec=" + latest_fork_usec +
                ", instantaneous_input_kbps=" + instantaneous_input_kbps +
                ", instantaneous_output_kbps=" + instantaneous_output_kbps +
                ", rejected_connections=" + rejected_connections +
                ", sync_full=" + sync_full +
                ", sync_partial_ok=" + sync_partial_ok +
                ", sync_partial_err=" + sync_partial_err +
                ", migrate_cached_sockets=" + migrate_cached_sockets +
                '}';
    }
}
