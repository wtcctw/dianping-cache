package com.dianping.cache.alarm.controller.mapper;

import com.dianping.cache.alarm.controller.dto.AlarmConfigDto;
import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.AlarmConfig;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class AlarmConfigMapper {

    public static AlarmConfig convertToAlarmConfig(AlarmConfigDto alarmConfigDto) {
        AlarmConfig alarmConfig = new AlarmConfig();
        int id = alarmConfigDto.getId();
        if (-1 != id) {
            alarmConfig.setId(alarmConfigDto.getId());
        }
        alarmConfig.setClusterType(alarmConfigDto.getClusterType());
        alarmConfig.setClusterName(alarmConfigDto.getClusterName());
        alarmConfig.setAlarmType(alarmConfigDto.getAlarmType());
        alarmConfig.setAlarmRule(alarmConfigDto.getAlarmRule());
        alarmConfig.setThreshold(alarmConfigDto.getThreshold());
        alarmConfig.setReceiver(alarmConfigDto.getReceiver());
        alarmConfig.setMailMode(alarmConfigDto.isMailMode());
        alarmConfig.setSmsMode(alarmConfigDto.isSmsMode());
        alarmConfig.setWeixinMode(alarmConfigDto.isWeixinMode());
        alarmConfig.setToBusiness(alarmConfigDto.isToBusiness());
        alarmConfig.setCreateTime(alarmConfigDto.getCreateTime());
        alarmConfig.setUpdateTime(alarmConfigDto.getUpdateTime());

        return alarmConfig;
    }


}
