package com.dianping.cache.monitor;

import com.dianping.cache.entity.CacheConfiguration;

public interface ServiceListener {

    public void serviceChanged(CacheConfiguration cacheConfig);

    public void serviceAdded(CacheConfiguration config);

    public void serviceRemoved(CacheConfiguration config);
    
}
