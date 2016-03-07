package com.dianping.cache.alarm.dataanalyse.minValCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvshiyun on 16/2/19.
 */
public class MinValCache {

    private static Map<String,MinVal> minValMap = new HashMap<String, MinVal>();

    private static MinValCache INSTANCE = new MinValCache();

    public static MinValCache getInstance(){
        return INSTANCE;
    }

    public static void setInstance(MinValCache minValCache){
        INSTANCE = minValCache;
    }

    void putToMinValMap(String name, MinVal minVal){
        MinVal newMinVal = new MinVal(minVal);
        minValMap.put(name,newMinVal);
    }

    MinVal getFromMinValMap(String name){
        return minValMap.get(name);
    }

}
