package com.dianping.squirrel.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.client.impl.DefaultStoreClient;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class StoreClientFactory {

	private static StoreClient storeClient = null;

	private static StoreClientConfigManager clientConfigManager = StoreClientConfigManager.getInstance();
	
	private static StoreCategoryConfigManager categoryConfigManager = StoreCategoryConfigManager.getInstance();
	
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
        StoreClient storeClient = clientConfigManager.findCacheClient(storeType);
        return storeClient;
    }
	
	public static StoreClient getStoreClientByCategory(String category) {
        checkNotNull(category, "store category is null");
        CacheKeyType categoryConfig = categoryConfigManager.findCacheKeyType(category);
        checkNotNull(categoryConfig, "%s's category config is null", category);
        checkNotNull(categoryConfig.getCacheType(), "%s's category store type is null", category);
        StoreClient storeClient = clientConfigManager.findCacheClient(categoryConfig.getCacheType());
        return storeClient;
    }
	
}
