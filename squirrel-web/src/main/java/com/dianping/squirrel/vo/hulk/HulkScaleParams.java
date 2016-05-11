package com.dianping.squirrel.vo.hulk;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public class HulkScaleParams {
    private String appkey;
    private String[] env;
    private String[] zone;
    private String num;
    private String image;
    private int cpu;
    private int mem;
    private int timeout;

    public HulkScaleParams() {
        this.image = null;
        this.cpu = 4;
        this.timeout = 60;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String[] getEnv() {
        return env;
    }

    public void setEnv(String[] env) {
        this.env = env;
    }

    public String[] getZone() {
        return zone;
    }

    public void setZone(String[] zone) {
        this.zone = zone;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
