package com.dianping.cache.alarm.entity;

import java.io.Serializable;

public class RedisBaseline implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6982505979154110644L;

    private int id;

    private String baseline_name;

    private int serverId;//对应的机器ip

    private long memory_used;

    private long curr_time;

    private int total_connections;

    private int connected_clients;

    private int qps;

    private double input_kbps;

    private double output_kbps;

    private double used_cpu_sys;

    private double used_cpu_user;

    private double used_cpu_sys_children;

    private double used_cpu_user_children;

    private int taskId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBaseline_name() {
        return baseline_name;
    }

    public RedisBaseline setBaseline_name(String baseline_name) {
        this.baseline_name = baseline_name;
        return this;
    }

    public int getServerId() {
        return serverId;
    }

    public RedisBaseline setServerId(int serverId) {
        this.serverId = serverId;
        return this;
    }

    public long getMemory_used() {
        return memory_used;
    }

    public RedisBaseline setMemory_used(long memory_used) {
        this.memory_used = memory_used;
        return this;
    }

    public long getCurr_time() {
        return curr_time;
    }

    public RedisBaseline setCurr_time(long curr_time) {
        this.curr_time = curr_time;
        return this;
    }

    public int getTotal_connections() {
        return total_connections;
    }

    public RedisBaseline setTotal_connections(int total_connections) {
        this.total_connections = total_connections;
        return this;
    }

    public int getConnected_clients() {
        return connected_clients;
    }

    public RedisBaseline setConnected_clients(int connected_clients) {
        this.connected_clients = connected_clients;
        return this;
    }

    public int getQps() {
        return qps;
    }

    public RedisBaseline setQps(int qps) {
        this.qps = qps;
        return this;
    }

    public double getInput_kbps() {
        return input_kbps;
    }

    public RedisBaseline setInput_kbps(double input_kbps) {
        this.input_kbps = input_kbps;
        return this;
    }

    public double getOutput_kbps() {
        return output_kbps;
    }

    public RedisBaseline setOutput_kbps(double output_kbps) {
        this.output_kbps = output_kbps;
        return this;
    }

    public double getUsed_cpu_sys() {
        return used_cpu_sys;
    }

    public RedisBaseline setUsed_cpu_sys(double used_cpu_sys) {
        this.used_cpu_sys = used_cpu_sys;
        return this;
    }

    public double getUsed_cpu_user() {
        return used_cpu_user;
    }

    public RedisBaseline setUsed_cpu_user(double used_cpu_user) {
        this.used_cpu_user = used_cpu_user;
        return this;
    }

    public double getUsed_cpu_sys_children() {
        return used_cpu_sys_children;
    }

    public RedisBaseline setUsed_cpu_sys_children(double used_cpu_sys_children) {
        this.used_cpu_sys_children = used_cpu_sys_children;
        return this;
    }

    public double getUsed_cpu_user_children() {
        return used_cpu_user_children;
    }

    public RedisBaseline setUsed_cpu_user_children(double used_cpu_user_children) {
        this.used_cpu_user_children = used_cpu_user_children;
        return this;
    }

    public int getTaskId() {
        return taskId;
    }

    public RedisBaseline setTaskId(int taskId) {
        this.taskId = taskId;
        return this;
    }
}
