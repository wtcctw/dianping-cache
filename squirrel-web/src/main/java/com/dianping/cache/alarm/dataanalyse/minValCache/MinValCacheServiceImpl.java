package com.dianping.cache.alarm.dataanalyse.minValCache;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by lvshiyun on 16/3/7.
 */
@Component
public class MinValCacheServiceImpl implements MinValCacheService {

    private static final int MEMUSAGE = 0;
    private static final int QPS = 1;
    private static final int CONN = 2;

    @Override
    public MinVal getMinValByName(String name) {
        return MinValCache.getInstance().getFromMinValMap(name);
    }

    @Override
    public void updateMinVal(String name, MinVal minVal) {
        MinValCache.getInstance().putToMinValMap(name,minVal);
    }

    @Override
    public boolean checkForUpdate(String name, MinVal minVal) {

        int type = MinValCache.getInstance().getFromMinValMap(name).getType();
        switch (type){
            case MEMUSAGE:
                return Float.parseFloat(MinValCache.getInstance().getFromMinValMap(name).getVal().toString())  > Float.parseFloat(minVal.getVal().toString()) ;
            case QPS:
                return Long.parseLong(MinValCache.getInstance().getFromMinValMap(name).getVal().toString())  > Long.parseLong(minVal.getVal().toString());
            case CONN:
                return Long.parseLong(MinValCache.getInstance().getFromMinValMap(name).getVal().toString())  > Long.parseLong(minVal.getVal().toString());
            default:
                return true;
        }
    }

    @Override
    public boolean isExpire(String name, int timeScope) {
        Date now = new Date();

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(now);
        gc.add(12,0-timeScope);

        long boundTime = gc.getTime().getTime();

        long minValTime = MinValCache.getInstance().getFromMinValMap(name).getTime().getTime();

        if(boundTime>minValTime){
            return true;
        }else {
            return false;
        }
    }
}
