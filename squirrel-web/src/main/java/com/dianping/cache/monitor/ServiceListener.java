package com.dianping.cache.monitor;

import com.dianping.cache.entity.CacheConfiguration;

public interface ServiceListener {

    void serviceChanged(CacheConfiguration cacheConfig);

    void serviceAdded(CacheConfiguration config);

    void serviceRemoved(CacheConfiguration config);
    
}
