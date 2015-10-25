/**
 * Project: avatar
 * 
 * File Created at 2010-7-13
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.squirrel.client.config;

import java.util.Set;

import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.core.CacheClientConfiguration;

/**
 * Cache service factory, it can retrieve available cache keys and find
 * {@link CacheService} implementation by cache key.
 * 
 * @author guoqing.chen
 * 
 */
public interface CacheClientFactory {

	/**
	 * Retrieve all available cache keys
	 */
	Set<String> getCacheClientKeys();

	/**
	 * Retrieve a {@link CacheService} instance by key
	 */
	CacheClient findCacheClient(String cacheKey);

	CacheClient init(String cacheKey);
	
	/**
	 * Get current {@link CacheClientConfiguration}
	 * 
	 * @param cacheKey
	 * @return
	 */
	CacheClientConfiguration getCacheClientConfig(String cacheKey);

}
