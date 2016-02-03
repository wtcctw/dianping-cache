package com.dianping.cache.alarm.controller.mapper;

import com.dianping.cache.alarm.controller.dto.MemcacheTemplateDto;
import com.dianping.cache.alarm.entity.MemcacheTemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class MemcacheTemplateMapper {

    public static MemcacheTemplate convertToMemcacheTemplate(MemcacheTemplateDto memcacheTemplateDto) {

        MemcacheTemplate memcacheTemplate = new MemcacheTemplate();

        if (-1 != memcacheTemplateDto.getId()) {
            memcacheTemplate.setId(memcacheTemplateDto.getId());
        }
        memcacheTemplate
                .setTemplateName(memcacheTemplateDto.getTemplateName())
                .setMailMode(memcacheTemplateDto.isMailMode())
                .setSmsMode(memcacheTemplateDto.isSmsMode())
                .setWeixinMode(memcacheTemplateDto.isWeixinMode());
        memcacheTemplate.setIsDown(memcacheTemplateDto.isDown())
                .setCheckHistory(memcacheTemplateDto.isCheckHistory())
                .setMemThreshold(memcacheTemplateDto.getMemThreshold())
                .setMemBase(memcacheTemplateDto.getMemBase())
                .setMemFluc(memcacheTemplateDto.getMemFluc())
                .setMemInterval(memcacheTemplateDto.getMemInterval())
                .setQpsThreshold(memcacheTemplateDto.getQpsThreshold())
                .setQpsBase(memcacheTemplateDto.getQpsBase())
                .setQpsFluc(memcacheTemplateDto.getQpsFluc())
                .setQpsInterval(memcacheTemplateDto.getQpsInterval())
                .setConnThreshold(memcacheTemplateDto.getConnThreshold())
                .setConnBase(memcacheTemplateDto.getConnBase())
                .setConnFluc(memcacheTemplateDto.getConnFluc())
                .setConnInterval(memcacheTemplateDto.getConnInterval())
                .setCreateTime(memcacheTemplateDto.getCreateTime())
                .setUpdateTime(new Date());

        return memcacheTemplate;
    }

}
