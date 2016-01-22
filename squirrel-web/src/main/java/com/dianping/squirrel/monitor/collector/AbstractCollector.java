package com.dianping.squirrel.monitor.collector;

import com.dianping.cache.util.ZKLeader;
import com.dianping.lion.Environment;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.monitor.data.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCollector {

    protected ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    @Autowired
    protected DataManager dataManager;

    protected boolean isProductEnv() {
        return "product".equals(Environment.getEnv());
    }

    protected boolean isLeader(){
        return ZKLeader.isLeader();
    }
}
