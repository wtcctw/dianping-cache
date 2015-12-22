package com.dianping.cache.scale1.cluster.redis;

public class RedisInfo {

    private long usedMemory;

    private long maxMemory;

    private int maxMem;

    private float used;

    private int qps;

    private int total_connections;

    private int connected_clients;

    private double input_kbps;

    private double output_kbps;

    private double used_cpu_sys;

    private double used_cpu_user;

    private double used_cpu_sys_children;

    private double used_cpu_user_children;

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
        setMaxMem((int) (maxMemory/1024));
    }

    public float getUsed() {
        return used;
    }

    public void setUsed(float used) {
        this.used = used;
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

    public int getQps() {
        return qps;
    }

    public void setQps(int qps) {
        this.qps = qps;
    }

    public int getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(int maxMem) {
        this.maxMem = maxMem;
    }

    public void calculateUsed() {
        used = (float) usedMemory / maxMemory;
        int tmp = Math.round(used * 10000);
        used = (float) (tmp / 100.0);
    }
}
