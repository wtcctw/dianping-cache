package com.dianping.cache.alarm.dataanalyse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvshiyun on 15/12/30.
 */
public class BaselineDict {

    Map<String, Baseline> baselineMap = new HashMap<String, Baseline>();

    private static final BaselineDict INSTANCE = new BaselineDict();

    public static BaselineDict getInstance() {
        return INSTANCE;
    }

    public BaselineDict() {
        this.baselineMap = new HashMap<String, Baseline>();
    }

    public Baseline getBaseline(String name) {
        return this.baselineMap.get(name);
    }

    public void putBaseline(String name, Baseline baseline) {
        this.baselineMap.put(name, baseline);
    }
}
