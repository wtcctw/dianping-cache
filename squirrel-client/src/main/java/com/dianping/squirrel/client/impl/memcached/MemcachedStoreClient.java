package com.dianping.squirrel.client.impl.memcached;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.lifecycle.Locatable;

public interface MemcachedStoreClient extends StoreClient, Locatable {

    public <T> CASValue<T> gets(StoreKey key) throws StoreException;
    
    public CASResponse cas(StoreKey key, long casId, Object value) throws StoreException;
    
}
