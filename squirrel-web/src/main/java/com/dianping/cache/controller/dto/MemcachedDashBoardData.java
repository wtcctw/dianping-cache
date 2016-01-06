package com.dianping.cache.controller.dto;

import java.util.Map;

/**
 * Created by dp on 16/1/4.
 */
public class MemcachedDashBoardData  extends DashBoardData {

    public MemcachedDashBoardData(){

    }

    public MemcachedDashBoardData(Map<String, Map<String, Object>> dataMap){

    }
    public class SimpleAnalysisData{
        int qps;
        long maxMemory;
        float memoryUsage;
        boolean QPSAlarm;
        boolean usageAlarm;
        boolean clusterAlarm;
        String clusterName;
        public SimpleAnalysisData(){
        }
        public boolean analysis(){
            return true;
        }
    }
}
