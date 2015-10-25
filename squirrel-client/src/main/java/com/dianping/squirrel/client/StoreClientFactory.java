package com.dianping.squirrel.client;

import com.dianping.squirrel.client.config.RemoteCacheClientFactory;
import com.dianping.squirrel.client.impl.DefaultStoreClient;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class StoreClientFactory {

	private static StoreClient storeClient = null;

	private static RemoteCacheClientFactory storeClientFactory;
	
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
	
}
