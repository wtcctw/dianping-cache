package com.dianping.cache.entity;

import java.io.Serializable;

public class RedisStats implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6982505979154110644L;

    private int id;

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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public long getMemory_used() {
        return memory_used;
    }

    public void setMemory_used(long memory_used) {
        this.memory_used = memory_used;
    }

    public long getCurr_time() {
        return curr_time;
    }

    public void setCurr_time(long curr_time) {
        this.curr_time = curr_time;
    }

    public int getTotal_connections() {
        return total_connections;
    }

    public void setTotal_connections(int total_connections) {
        this.total_connections = total_connections;
    }

    public int getConnected_clients() {
        return connected_clients;
    }

    public void setConnected_clients(int connected_clients) {
        this.connected_clients = connected_clients;
    }

    public int getQps() {
        return qps;
    }

    public void setQps(int qps) {
        this.qps = qps;
    }

    public double getInput_kbps() {
        return input_kbps;
    }

    public void setInput_kbps(double input_kbps) {
        this.input_kbps = input_kbps;
    }

    public double getOutput_kbps() {
        return output_kbps;
    }

    public void setOutput_kbps(double output_kbps) {
        this.output_kbps = output_kbps;
    }

    public double getUsed_cpu_sys() {
        return used_cpu_sys;
    }

    public void setUsed_cpu_sys(double used_cpu_sys) {
        this.used_cpu_sys = used_cpu_sys;
    }

    public double getUsed_cpu_user() {
        return used_cpu_user;
    }

    public void setUsed_cpu_user(double used_cpu_user) {
        this.used_cpu_user = used_cpu_user;
    }

    public double getUsed_cpu_sys_children() {
        return used_cpu_sys_children;
    }

    public void setUsed_cpu_sys_children(double used_cpu_sys_children) {
        this.used_cpu_sys_children = used_cpu_sys_children;
    }

    public double getUsed_cpu_user_children() {
        return used_cpu_user_children;
    }

    public void setUsed_cpu_user_children(double used_cpu_user_children) {
        this.used_cpu_user_children = used_cpu_user_children;
    }
}
