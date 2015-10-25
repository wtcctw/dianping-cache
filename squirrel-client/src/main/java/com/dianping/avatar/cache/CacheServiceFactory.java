/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.avatar.cache;

import com.dianping.squirrel.common.exception.StoreInitializeException;

public class CacheServiceFactory {

	private static CacheService cacheService = null;

	static {
		try {
			cacheService = new DefaultCacheService();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new StoreInitializeException(e);
		}
	}

	public static CacheService getCacheService() {
		return cacheService;
	}
}
