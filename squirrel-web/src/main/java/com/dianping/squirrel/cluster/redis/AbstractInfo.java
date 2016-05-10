package com.dianping.squirrel.cluster.redis;

import com.dianping.squirrel.cluster.RedisUtil;

import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/10.
 */
public abstract class AbstractInfo {
    protected String infoSegmentName;
    protected Map<String,String> infoMap;

    public AbstractInfo() {
    }

    public AbstractInfo(Map<String, String> infoMap) {
        this.infoMap = infoMap;
        init();
    }

    public AbstractInfo(String info) {
        this.infoMap = RedisUtil.parseStringToMap(info);
        init();
    }

    public abstract void init();
}
