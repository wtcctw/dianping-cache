package com.dianping.cache.service;

import com.dianping.cache.controller.vo.RedisReshardParams;
import com.dianping.cache.scale.cluster.redis.ReshardPlan;

/**
 * Created by dp on 15/12/29.
 */
public interface ReshardService {
    ReshardPlan createReshardPlan(RedisReshardParams redisReshardParams);
    ReshardPlan findLastReshardPlan();
    ReshardPlan getPlanByPlanId(int id);
    void updateReshardPlan(ReshardPlan reshardPlan);
    void stopPlan(int id);
    void cancelPlan(int id);
}
