package com.dianping.cache.alarm.controller.dto;

/**
 * Created by lvshiyun on 15/12/9.
 */
public class BaseQueryDto {

    private int offset;

    private int limit;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
