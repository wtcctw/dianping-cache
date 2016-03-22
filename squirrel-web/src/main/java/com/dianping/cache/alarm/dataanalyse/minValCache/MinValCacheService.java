package com.dianping.cache.alarm.dataanalyse.minValCache;

/**
 * Created by lvshiyun on 15/12/26.
 */

public interface MinValCacheService {

    MinVal getMinValByName(String name);

    void updateMinVal(String name, MinVal minVal);

    boolean checkForUpdate(String name, MinVal minVal);

    boolean isExpire(String name, int timeScope);

}
