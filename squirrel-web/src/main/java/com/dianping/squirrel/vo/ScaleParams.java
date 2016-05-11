package com.dianping.squirrel.vo;

import java.util.Arrays;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class ScaleParams {
    private String clusterName;
    private String[] environment;
    private String[] zone;
    private int num;
    private int cpu;
    private int memory;

    public ScaleParams(String clusterName, String[] environment, String[] zone, int num, int cpu, int memory) {
        this.clusterName = clusterName;
        this.environment = environment;
        this.zone = zone;
        this.num = num;
        this.cpu = cpu;
        this.memory = memory;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String[] getEnvironment() {
        return environment;
    }

    public void setEnvironment(String[] environment) {
        this.environment = environment;
    }

    public String[] getZone() {
        return zone;
    }

    public void setZone(String[] zone) {
        this.zone = zone;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }


    @Override
    public String toString() {
        return "ScaleParams{" +
                "clusterName='" + clusterName + '\'' +
                ", environment=" + Arrays.toString(environment) +
                ", zone=" + Arrays.toString(zone) +
                ", num=" + num +
                ", cpu=" + cpu +
                ", memory=" + memory +
                '}';
    }
}

