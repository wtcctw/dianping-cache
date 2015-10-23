package com.dianping.squirrel;

import com.dianping.cache.exception.InitializingException;
import com.dianping.squirrel.impl.DefaultStoreClient;

public class StoreClientFactory {

	private static StoreClient storeClient = null;

	static {
		try {
			storeClient = new DefaultStoreClient();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new InitializingException(e);
		}
	}

	public static StoreClient getStoreClient() {
		return storeClient;
	}
	
}
