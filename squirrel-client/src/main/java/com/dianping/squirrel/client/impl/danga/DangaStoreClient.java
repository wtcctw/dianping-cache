package com.dianping.squirrel.client.impl.danga;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.exception.StoreException;

/**
 * Created by dp on 15/11/30.
 */
public interface DangaStoreClient extends StoreClient{
	public <T> CASValue<T> gets(StoreKey key) throws StoreException;
	public boolean cas(StoreKey key, long casId, Object value) throws StoreException;
}
