/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.avatar.cache;

import com.dianping.cache.exception.InitializingException;

public class CacheServiceFactory {

	private static CacheService cacheService = null;

	static {
		try {
			cacheService = new DefaultCacheService();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new InitializingException(e);
		}
	}

	public static CacheService getCacheService() {
		return cacheService;
	}
}
