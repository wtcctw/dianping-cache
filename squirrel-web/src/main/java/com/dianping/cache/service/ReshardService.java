package com.dianping.cache.service;

import com.dianping.cache.scale.cluster.redis.ReshardPlan;

import java.util.List;

/**
 * Created by dp on 15/12/29.
 */
public interface ReshardService {
    ReshardPlan createReshardPlan(String cluster,List<String> srcNodes, List<String> desNodes, boolean isAverage);
    ReshardPlan findLastReshardPlan();
    ReshardPlan getPlanByPlanId(int id);
    void updateReshardPlan(ReshardPlan reshardPlan);
    void stopPlan(int id);
    void cancelPlan(int id);
}
