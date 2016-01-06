package com.dianping.cache.controller.dto;

import java.util.List;

/**
 * Created by dp on 16/1/6.
 */
public class RedisReshardParams {
    private boolean isAverage;
    private List<String> srcNodes;
    private List<String> desNodes;

    public boolean isAverage() {
        return isAverage;
    }

    public void setIsAverage(boolean average) {
        isAverage = average;
    }

    public List<String> getSrcNodes() {
        return srcNodes;
    }

    public void setSrcNodes(List<String> srcNodes) {
        this.srcNodes = srcNodes;
    }

    public List<String> getDesNodes() {
        return desNodes;
    }

    public void setDesNodes(List<String> desNodes) {
        this.desNodes = desNodes;
    }
}
