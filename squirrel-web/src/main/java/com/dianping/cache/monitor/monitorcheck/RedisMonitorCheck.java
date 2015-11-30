package com.dianping.cache.monitor.monitorcheck;

import com.dianping.cache.entity.RedisMonitorConfig;
import com.dianping.cache.monitor.statsdata.RedisClusterData;

/**
 * Created by dp on 15/11/23.
 */
public class RedisMonitorCheck {

    private RedisMonitorConfig config;

    private int alarm = 1;

    private final int WARN = 2;

    private final int DANGER = 4;

    public void check(RedisClusterData data) {
        if(null == config){//default
            config = new RedisMonitorConfig();
        }
        if(data.getMaxMemory() == 0L)
            alarm = 4;
        data.getColors().put("used", switchColors(data.getUsed(),config.getMemUsedWarn(),config.getMemUsedDanger()));
        data.getColors().put("alarm",switchColors(alarm,WARN,DANGER));
    }

    private String switchColors(int value,int warn,int danger){
         if(value >= danger){
             alarm |= DANGER;
             return "red";
         } else if(value >= warn){
             alarm |= WARN;
             return "orange";
         }
        return "green";
    }

    private String switchColors(float value,float warn,float danger){
        if(value >= danger){
            alarm |= DANGER;
            return "red";
        } else if(value >= warn){
            alarm |= WARN;
            return "orange";
        }
        return "green";
    }

    private String switchColors(long value,long warn,long danger){
        if(value >= danger){
            alarm |= DANGER;
            return "red";
        } else if(value >= warn){
            alarm |= WARN;
            return "orange";
        }
        return "green";
    }
}
