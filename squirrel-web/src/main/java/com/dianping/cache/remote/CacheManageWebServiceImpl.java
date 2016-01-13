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

import com.dianping.avatar.exception.DuplicatedIdentityException;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.CacheKeyConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.remote.cache.dto.CacheClearDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.GenericCacheConfigurationDTO;
import com.dianping.squirrel.client.util.DTOUtils;

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

	@Override
	public void createConfiguration(GenericCacheConfigurationDTO configuration) {
		try {
			CacheConfiguration config = new CacheConfiguration();
			DTOUtils.copyProperties(config, configuration);
			cacheConfigurationService.create(config);
		} catch (DuplicatedIdentityException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void updateConfiguration(GenericCacheConfigurationDTO configuration) {
		CacheConfiguration config = new CacheConfiguration();
		DTOUtils.copyProperties(config, configuration);
		cacheConfigurationService.update(config);
	}

	@Override
	public void createCacheKeyConfig(CacheKeyConfigurationDTO configuration) {
	    validateCacheKeyConfig(configuration);
		try {
			CacheKeyConfiguration config = new CacheKeyConfiguration();
			DTOUtils.copyProperties(config, configuration);
			cacheKeyConfigurationService.create(config);
		} catch (DuplicatedIdentityException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void updateCacheKeyConfig(CacheKeyConfigurationDTO configuration) {
	    validateCacheKeyConfig(configuration);
		CacheKeyConfiguration config = new CacheKeyConfiguration();
		DTOUtils.copyProperties(config, configuration);
		cacheKeyConfigurationService.update(config);
	}

	private void validateCacheKeyConfig(CacheKeyConfigurationDTO configuration) {
        if(configuration == null) {
            throw new RuntimeException("cache category config is null");
        }
        validateCacheKeyCategory(configuration.getCategory());
        validateCacheKeyDuration(configuration.getDuration());
    }

    private void validateCacheKeyCategory(String category) {
        if(category == null || category.trim().length() == 0) {
            throw new RuntimeException("cache category name is empty");
        }
    }

    private void validateCacheKeyDuration(String duration) {
        if(duration == null) {
            throw new RuntimeException("cache category duration is null");
        }
        if(duration.endsWith("m") || duration.endsWith("h") || duration.endsWith("s")) {
            duration = duration.substring(0, duration.length() - 1);
        }
        try {
            Integer.parseInt(duration);
        } catch(NumberFormatException e) {
            throw new RuntimeException("cache category duration is invalid: " + duration);
        }
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
