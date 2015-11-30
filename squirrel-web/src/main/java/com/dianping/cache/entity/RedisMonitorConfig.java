package com.dianping.cache.entity;

/**
 * Created by dp on 15/11/23.
 */
public class RedisMonitorConfig {

    private int id;
    private float memUsedWarn = 50.0f;
    private float memUsedDanger = 90.0f;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getMemUsedWarn() {
        return memUsedWarn;
    }

    public void setMemUsedWarn(float memUsedWarn) {
        this.memUsedWarn = memUsedWarn;
    }

    public float getMemUsedDanger() {
        return memUsedDanger;
    }

    public void setMemUsedDanger(float memUsedDanger) {
        this.memUsedDanger = memUsedDanger;
    }

}
