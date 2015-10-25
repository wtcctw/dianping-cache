/**
 * Project: avatar-cache
 * 
 * File Created at 2011-9-13
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.squirrel.client.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.squirrel.client.util.CacheMonitorUtil;

/**
 * Enhanced <code>CacheItemConfigManager</code>, 用于对未配置正确的cache category进行容错和告警
 * 
 * @author danson.liu
 * 
 */
public class EnhancedCacheItemConfigManager implements CacheItemConfigManager {

	private CacheItemConfigManager itemConfigManager;

	private static ConcurrentMap<String, CacheKeyType> DEFAULT_CACHE_KEY_TYPES = new ConcurrentHashMap<String, CacheKeyType>();

	public EnhancedCacheItemConfigManager(CacheItemConfigManager itemConfigManager) {
		this.itemConfigManager = itemConfigManager;
	}

	@Override
	public CacheKeyType init(String category) {
		return getCacheKeyType(category);
	}

	@Override
	public CacheKeyType getCacheKeyType(String category) {
		CacheKeyType cacheKeyType = itemConfigManager.getCacheKeyType(category);
		if (cacheKeyType != null) {
			return cacheKeyType;
		}
		CacheMonitorUtil.logConfigNotFound(category, "Cache config[category=" + category + "] not found", 500);
		CacheKeyType defaultCacheKeyType = DEFAULT_CACHE_KEY_TYPES.get(category);
		if (defaultCacheKeyType != null) {
			return defaultCacheKeyType;
		}
		defaultCacheKeyType = new DefaultCacheKeyType(category);
		DEFAULT_CACHE_KEY_TYPES.put(category, defaultCacheKeyType);
		return defaultCacheKeyType;
	}

	@Override
	public Set<String> getCacheItemKeys() {
		return itemConfigManager.getCacheItemKeys();
	}

	@Override
	public CacheKeyType findCacheKeyType(String category) {
		CacheKeyType type = itemConfigManager.findCacheKeyType(category);
		if (type == null) {
			CacheKeyType defaultCacheKeyType = DEFAULT_CACHE_KEY_TYPES.get(category);
			if (defaultCacheKeyType != null) {
				return defaultCacheKeyType;
			}
			defaultCacheKeyType = new DefaultCacheKeyType(category);
			DEFAULT_CACHE_KEY_TYPES.put(category, defaultCacheKeyType);
			return defaultCacheKeyType;
		}
		return type;
	}

	@Override
	public void removeCacheKeyType(String category) {
		itemConfigManager.removeCacheKeyType(category);
	}

}
