package com.dianping.cache.alarm.controller.mapper;

import com.dianping.cache.alarm.controller.dto.RedisTemplateDto;
import com.dianping.cache.alarm.entity.RedisTemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class RedisTemplateMapper {

    public static RedisTemplate convertToRedisTemplate(RedisTemplateDto redisTemplateDto) {

        RedisTemplate redisTemplate = new RedisTemplate();
        if(-1 != redisTemplateDto.getId()){
            redisTemplate.setId(redisTemplateDto.getId());
        }
        redisTemplate.setClusterName(redisTemplateDto.getClusterName());
        redisTemplate.setIsDown(redisTemplateDto.isDown())
                .setMemThreshold(redisTemplateDto.getMemThreshold())
                .setCreateTime(redisTemplateDto.getCreateTime())
                .setUpdateTime(new Date());

        return redisTemplate;
    }

}
