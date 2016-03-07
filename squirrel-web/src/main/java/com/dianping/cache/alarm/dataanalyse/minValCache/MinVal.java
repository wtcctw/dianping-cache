package com.dianping.cache.alarm.dataanalyse.minValCache;

import java.util.Date;

/**
 * Created by lvshiyun on 16/3/7.
 */
public class MinVal {

    private String category;

    private int type;

    private Date time;

    private Object val;


    public MinVal( MinVal minVal){
        this.category = minVal.getCategory();
        this.type = minVal.getType();
        this.time = minVal.getTime();
        this.val = minVal.getVal();
    }

    public MinVal(String category, int type, Date time, Object val){
        this.category = category;
        this.type= type;
        this.time = time;
        this.val = val;
    }


    public String getCategory() {
        return category;
    }

    public MinVal setCategory(String category) {
        this.category = category;
        return this;
    }

    public int getType() {
        return type;
    }

    public MinVal setType(int type) {
        this.type = type;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public MinVal setTime(Date time) {
        this.time = time;
        return this;
    }

    public Object getVal() {
        return val;
    }

    public MinVal setVal(Object val) {
        this.val = val;
        return this;
    }
}
