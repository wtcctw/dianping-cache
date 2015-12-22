package com.dianping.cache.alarm.controller.mapper;

import com.dianping.cache.alarm.controller.dto.RedisAlarmConfigDto;
import com.dianping.cache.alarm.entity.RedisAlarmConfig;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class RedisAlarmConfigMapper {

    public static RedisAlarmConfig convertToRedisAlarmConfig(RedisAlarmConfigDto RedisAlarmConfigDto){
        RedisAlarmConfig RedisAlarmConfig = new RedisAlarmConfig();
        RedisAlarmConfig.setRedisKey(RedisAlarmConfigDto.getRedisKey());
        RedisAlarmConfig.setAlarmSwitch(RedisAlarmConfigDto.isAlarmSwitch());
        RedisAlarmConfig.setMemThreshold(RedisAlarmConfigDto.getMemThreshold());
        RedisAlarmConfig.setQpsThreshold(RedisAlarmConfigDto.getQpsThreshold());
        RedisAlarmConfig.setConnThreshold(RedisAlarmConfigDto.getConnThreshold());
        RedisAlarmConfig.setCreateTime(RedisAlarmConfigDto.getCreateTime());
        RedisAlarmConfig.setUpdateTime(RedisAlarmConfigDto.getUpdateTime());

        return RedisAlarmConfig;
    }


}
