package com.dianping.cache.alarm.controller.dto;

import java.util.List;

/**
 * Created by lvshiyun on 15/12/6.
 */
public class AlarmMetaBatchDto {

    private List<Integer> metaIds;

    private UpdateType updateType;

    private boolean isOpen;

    public List<Integer>getMetaIds(){
        return metaIds;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public enum UpdateType{
        SMS,WEIXIN,MAIL;
    }
}
