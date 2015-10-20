/**
 * Project: avatar
 * 
 * File Created at 2010-10-15
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
package com.dianping.avatar.cache.configuration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.client.RemoteCacheClientFactory;
import com.dianping.avatar.cache.jms.CacheCuratorClient;
import com.dianping.avatar.cache.util.DTOUtils;
import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.exception.CacheException;
import com.dianping.cache.util.ZKUtils;
import com.dianping.cat.Cat;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.remote.cache.CacheConfigurationWebService;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;

/**
 * Remote centralized managed cache item config
 * 
 * @author danson.liu
 * 
 */
public class RemoteCacheItemConfigManager implements CacheItemConfigManager {

	private static transient Logger logger = LoggerFactory.getLogger(RemoteCacheItemConfigManager.class);

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();

	private CacheConfigurationWebService configurationWebService;

	private ConcurrentMap<String, CacheKeyType> cacheKeyTypes = new ConcurrentHashMap<String, CacheKeyType>();

	private Set<String> usedCategories = new ConcurrentSkipListSet<String>();

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static RemoteCacheItemConfigManager instance;
    
    private RemoteCacheItemConfigManager() {
        try {
            init();
        } catch (Exception e) {
            logger.error("failed to init cache item config manager", e);
        }
    }
    
    public static RemoteCacheItemConfigManager getInstance() {
        if(instance == null) {
            synchronized(RemoteCacheClientFactory.class) {
                if(instance == null) {
                    instance = new RemoteCacheItemConfigManager();
                }
            }
        }
        return instance;
    }
	    
	@Override
	public CacheKeyType getCacheKeyType(String category) {
		return cacheKeyTypes.get(category);
	}

	@Override
	public CacheKeyType init(String category) {
		CacheKeyType cacheKeyType = cacheKeyTypes.get(category);
		if (cacheKeyType == null) {
			synchronized (this) {
				cacheKeyType = cacheKeyTypes.get(category);
				if (cacheKeyType == null) {
					CacheKeyConfigurationDTO categoryConfig = loadCacheCategoryConfig(category);
					if (categoryConfig == null) {
						logger.error("failed to get category config: " + category);
						Cat.logError(new CacheException("failed to get category config: " + category));
						CacheKeyType defaultType = new DefaultCacheKeyType(category);
						cacheKeyTypes.put(category, defaultType);
						return defaultType;
					}
					logger.info("loaded category config: " + categoryConfig);
					cacheKeyType = registerCacheKey(categoryConfig);
				}
			}
		}
		return cacheKeyType;
	}

	@Override
	public CacheKeyType findCacheKeyType(String category) {
	    if(StringUtils.isBlank(category)) {
	        throw new NullPointerException("cache category is empty");
	    }
		if (!usedCategories.contains(category)) {
			usedCategories.add(category);
		}
		return init(category);
	}

	private CacheKeyConfigurationDTO loadCacheCategoryConfig(String category) {
		CacheKeyConfigurationDTO categoryConfig = null;
		try {
		    logger.debug("loading category config from zookeeper: " + category);
			categoryConfig = cacheCuratorClient.getCategoryConfig(category);
		} catch (Exception e) {
			logger.error("failed to get cache category config of " + category, e);
		}
		if (categoryConfig == null) {
		    logger.debug("loading category config from cache server: " + category);
			categoryConfig = configurationWebService.getKeyConfiguration(category);
		}
		return categoryConfig;
	}

	private CacheKeyConfigurationDTO getDefaultCategoryConfig(String category) {
	    CacheKeyConfigurationDTO categoryConfig = new CacheKeyConfigurationDTO();
        categoryConfig.setCategory(category);
        categoryConfig.setCacheType("memcached");
        categoryConfig.setIndexTemplate("{0}");
        categoryConfig.setDuration("2");
        categoryConfig.setIndexDesc("");
        categoryConfig.setSync2Dnet(false);
        categoryConfig.setHot(false);
        return categoryConfig;
    }

    /**
	 * 
	 */
	private void pollConfigurationFromServer() {
		try {
			cacheKeyTypes.clear();
			List<CacheKeyConfigurationDTO> allConfigurations = configurationWebService.getKeyConfigurations();
			for (CacheKeyConfigurationDTO configurationDTO : allConfigurations) {
				registerCacheKey(configurationDTO);
			}
		} catch (Exception e) {
			String errorMsg = "Poll cache key configuration from cache server failed.";
			logger.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	/**
	 * @param configurationDTO
	 */
	public void updateConfig(CacheKeyConfigurationDTO configurationDTO) {
		registerCacheKey(configurationDTO);
	}

	/**
	 * @param configurationDTO
	 */
	private CacheKeyType registerCacheKey(CacheKeyConfigurationDTO configurationDTO) {
		CacheKeyType cacheKeyType = new CacheKeyType();
		DTOUtils.copyProperties(cacheKeyType, configurationDTO);
		cacheKeyTypes.put(cacheKeyType.getCategory(), cacheKeyType);
		return cacheKeyType;
	}

	@Override
	public Set<String> getCacheItemKeys() {
		return usedCategories;
	}

	@Override
	public void removeCacheKeyType(String category) {
		cacheKeyTypes.remove(category);
	}

	public void init() throws Exception {
	    configurationWebService = ServiceFactory.getService(
                "http://service.dianping.com/cacheService/cacheConfigService_1.0.0",
                CacheConfigurationWebService.class, 10000);
		initCacheCategories();
	}
	
	private void initCacheCategories() {
	    if (ZKUtils.isZookeeperEnabled()) {
            String appName = configManager.getAppName();
            if (StringUtils.isNotEmpty(appName)) {
                try {
                    String categories = cacheCuratorClient.getRuntimeCategories(appName);
                    if (StringUtils.isNotEmpty(categories)) {
                        logger.info("initializing cache categories: " + categories);
                        String[] cacheCategories = StringUtils.split(categories, ',');
                        for (String cacheCategory : cacheCategories) {
                            if(StringUtils.isNotBlank(cacheCategory)) {
                                init(cacheCategory.trim());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("failed to initialize cache categories", e);
                }
            }
        }
	}

}
