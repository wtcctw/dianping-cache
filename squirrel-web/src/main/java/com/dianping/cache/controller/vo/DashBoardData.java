package com.dianping.cache.controller.vo;

/**
 * Created by dp on 16/1/4.
 */
public class DashBoardData {
    private int totalNum;
    private int healthyNum;
    private int dangerNum;

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getHealthyNum() {
        return healthyNum;
    }

    public void setHealthyNum(int healthyNum) {
        this.healthyNum = healthyNum;
    }

    public int getDangerNum() {
        return dangerNum;
    }

    public void setDangerNum(int dangerNum) {
        this.dangerNum = dangerNum;
    }
}
