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
        memcacheTemplate.setClusterName(memcacheTemplateDto.getClusterName());
        memcacheTemplate.setIsDown(memcacheTemplateDto.isDown())
                .setMemThreshold(memcacheTemplateDto.getMemThreshold())
                .setQpsThreshold(memcacheTemplateDto.getQpsThreshold())
                .setConnThreshold(memcacheTemplateDto.getConnThreshold())
                .setCreateTime(memcacheTemplateDto.getCreateTime())
                .setUpdateTime(new Date());

        return memcacheTemplate;
    }

}
