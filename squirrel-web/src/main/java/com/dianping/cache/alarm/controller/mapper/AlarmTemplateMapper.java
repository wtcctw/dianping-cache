package com.dianping.cache.alarm.controller.mapper;

import com.dianping.cache.alarm.controller.dto.AlarmTemplateDto;
import com.dianping.cache.alarm.entity.AlarmTemplate;

import java.util.Date;

/**
 * Created by lvshiyun on 15/12/24.
 */
public class AlarmTemplateMapper {

    public static AlarmTemplate convertToAlarmTemplate(AlarmTemplateDto alarmTemplateDto) {

        AlarmTemplate alarmTemplate = new AlarmTemplate();
        if(-1 != alarmTemplateDto.getId()){
            alarmTemplate.setId(alarmTemplateDto.getId());
        }
        alarmTemplate
                .setTemplateName(alarmTemplateDto.getTemplateName())
                .setMailMode(alarmTemplateDto.isMailMode())
                .setSmsMode(alarmTemplateDto.isSmsMode())
                .setWeixinMode(alarmTemplateDto.isWeixinMode());
        alarmTemplate.setAlarmType(alarmTemplateDto.getAlarmType())
                .setAlarmSwitch(alarmTemplateDto.isAlarmSwitch())
                .setThreshold(alarmTemplateDto.getThreshold())
                .setFlucSwitch(alarmTemplateDto.isFlucSwitch())
                .setFluc(alarmTemplateDto.getFluc())
                .setBase(alarmTemplateDto.getBase())
                .setAlarmInterval(alarmTemplateDto.getAlarmInterval())
                .setCreateTime(alarmTemplateDto.getCreateTime())
                .setUpdateTime(new Date());

        return alarmTemplate;
    }

}
