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
        redisTemplate
                .setTemplateName(redisTemplateDto.getTemplateName())
                .setMailMode(redisTemplateDto.isMailMode())
                .setSmsMode(redisTemplateDto.isSmsMode())
                .setWeixinMode(redisTemplateDto.isWeixinMode());
        redisTemplate.setIsDown(redisTemplateDto.isDown())
                .setCheckHistory(redisTemplateDto.isCheckHistory())
                .setMemSwitch(redisTemplateDto.isMemSwitch())
                .setMemThreshold(redisTemplateDto.getMemThreshold())
                .setMemBase(redisTemplateDto.getMemBase())
                .setMemFluc(redisTemplateDto.getMemFluc())
                .setMemInterval(redisTemplateDto.getMemInterval())
                .setQpsSwitch(redisTemplateDto.isQpsSwitch())
                .setQpsThreshold(redisTemplateDto.getQpsThreshold())
                .setQpsBase(redisTemplateDto.getQpsBase())
                .setQpsFluc(redisTemplateDto.getQpsFluc())
                .setQpsInterval(redisTemplateDto.getQpsInterval())
                .setCreateTime(redisTemplateDto.getCreateTime())
                .setUpdateTime(new Date());

        return redisTemplate;
    }

}
