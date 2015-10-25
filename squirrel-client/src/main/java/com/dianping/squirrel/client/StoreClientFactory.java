package com.dianping.squirrel.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dianping.squirrel.client.config.CacheClientFactory;
import com.dianping.squirrel.client.config.CacheItemConfigManager;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.RemoteCacheClientFactory;
import com.dianping.squirrel.client.config.RemoteCacheItemConfigManager;
import com.dianping.squirrel.client.impl.DefaultStoreClient;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class StoreClientFactory {

	private static StoreClient storeClient = null;

	private static CacheClientFactory clientFactory = RemoteCacheClientFactory.getInstance();
	
	private static CacheItemConfigManager categoryConfigManager = RemoteCacheItemConfigManager.getInstance();
	
	static {
		try {
			storeClient = new DefaultStoreClient();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new StoreInitializeException(e);
		}
	}

	public static StoreClient getStoreClient() {
		return storeClient;
	}
	
	public static StoreClient getStoreClient(String storeType) {
        checkNotNull(storeType, "store type is null");
        StoreClient storeClient = clientFactory.findCacheClient(storeType);
        return storeClient;
    }
	
	public static StoreClient getStoreClientByCategory(String category) {
        checkNotNull(category, "store category is null");
        CacheKeyType categoryConfig = categoryConfigManager.findCacheKeyType(category);
        checkNotNull(categoryConfig, "%s's category config is null", category);
        checkNotNull(categoryConfig.getCacheType(), "%s's category store type is null", category);
        StoreClient storeClient = clientFactory.findCacheClient(categoryConfig.getCacheType());
        return storeClient;
    }
	
}
