/**
 * Project: com.dianping.cache-server-2.0.0-SNAPSHOT
 * 
 * File Created at 2011-2-15
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
package com.dianping.cache.remote;

import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.squirrel.common.domain.CacheClearDTO;

/**
 * 用于缓存的管理，如清除缓存等
 * 
 * @author danson.liu
 * 
 */
public class CacheManageWebServiceImpl implements CacheManageWebService {

	private CacheConfigurationService cacheConfigurationService;

	private CacheKeyConfigurationService cacheKeyConfigurationService;

	@Override
	public void clearByCategory(String category, String serverOrGroup) {
		cacheConfigurationService.clearByCategory(category, serverOrGroup);
	}

	@Override
	public void clearByCategory(String category) {
		cacheConfigurationService.clearByCategory(category);
	}

	@Override
	public void clearByKey(String cacheType, String key) {
		cacheConfigurationService.clearByKey(cacheType, key);
	}

	@Override
	public void clearByKey(CacheClearDTO cacheClear) {
		cacheConfigurationService.clearByKey(cacheClear.getCacheType(), cacheClear.getKey());
	}

    /**
	 * @param cacheConfigurationService
	 *            the cacheConfigurationService to set
	 */
	public void setCacheConfigurationService(CacheConfigurationService cacheConfigurationService) {
		this.cacheConfigurationService = cacheConfigurationService;
	}

	/**
	 * @param cacheKeyConfigurationService
	 *            the cacheKeyConfigurationService to set
	 */
	public void setCacheKeyConfigurationService(CacheKeyConfigurationService cacheKeyConfigurationService) {
		this.cacheKeyConfigurationService = cacheKeyConfigurationService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.remote.cache.CacheManageWebService#incVersion(java.lang.
	 * String)
	 */
	@Override
	public void incVersion(String category) {
		cacheConfigurationService.incVersion(category);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.remote.cache.CacheManageWebService#pushCategoryConfig(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	public void pushCategoryConfig(String category, String serverOrGroup) {
		cacheConfigurationService.pushCategoryConfig(category, serverOrGroup);
	}

	@Override
	public void migrate() {
		cacheConfigurationService.migrate();
	}

	@Override
	public int getBucket(String category) {
		return category.hashCode() % 50 + 50;
	}
}
