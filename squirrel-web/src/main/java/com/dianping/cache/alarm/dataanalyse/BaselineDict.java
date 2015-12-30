package com.dianping.cache.alarm.dataanalyse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/30.
 */
public class BaselineDict {

    Map<String, Baseline> baselineMap;

    public BaselineDict() {
        this.baselineMap = new HashMap<String, Baseline>();
    }

    public Baseline getBaselineMap(String name) {
        return this.baselineMap.get(name);
    }

    public void putBaselineMap(String name, Baseline baseline) {
        this.baselineMap.put(name, baseline);
    }
}
