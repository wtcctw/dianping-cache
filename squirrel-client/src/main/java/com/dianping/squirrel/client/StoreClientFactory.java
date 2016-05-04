package com.dianping.squirrel.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dianping.squirrel.client.config.StoreCategoryConfig;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.client.impl.DefaultStoreClient;
import com.dianping.squirrel.client.log.LoggerLoader;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class StoreClientFactory {

	static {
		LoggerLoader.init();
	}

	private static StoreClient storeClient = null;

	private static StoreClientConfigManager clientConfigManager = StoreClientConfigManager.getInstance();

	private static StoreCategoryConfigManager categoryConfigManager = StoreCategoryConfigManager.getInstance();

	public static StoreClient getStoreClient() {
		if (storeClient == null) {
			synchronized (StoreClientFactory.class) {
				if (storeClient == null) {
					try {
						storeClient = new DefaultStoreClient();
					} catch (Throwable e) {
						throw new StoreInitializeException(e);
					}
				}
			}
		}
		return storeClient;
	}

	/**
	 * @deprecated use getStoreClientByCategory instead
	 * 
	 * @param storeType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends StoreClient> T getStoreClient(String storeType) {
		checkNotNull(storeType, "store type is null");
		return (T) clientConfigManager.findCacheClient(storeType);
	}

	@SuppressWarnings("unchecked")
	public static <T extends StoreClient> T getStoreClientByCategory(String category) {
		checkNotNull(category, "store category is null");
		StoreCategoryConfig categoryConfig = categoryConfigManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s's category config is null", category);
		checkNotNull(categoryConfig.getCacheType(), "%s's category store type is null", category);
		return (T) clientConfigManager.findCacheClient(categoryConfig.getCacheType());
	}
}
