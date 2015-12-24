package com.dianping.cache.alarm.lifecycle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lvshiyun on 15/11/30.
 */
public abstract class AbstractComponentMonitorable implements ComponentMonitable {

    @JsonIgnore
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getName() {

        return getClass().getName();
    }

    @Override
    public Object getStatus() {
        return this.toString();
    }

}
