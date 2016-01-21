package com.dianping.squirrel.monitor.collector;

import com.dianping.cache.util.ZKLeader;
import com.dianping.lion.Environment;
import com.dianping.squirrel.monitor.data.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCollector {

    private final String cron = "";

    @Autowired
    protected DataManager dataManager;

    protected boolean isProductEnv() {
        return "product".equals(Environment.getEnv());
    }

    protected boolean isLeader(){
        return ZKLeader.isLeader();
    }
}
