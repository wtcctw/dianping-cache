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
package com.dianping.squirrel.client.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import net.sf.ehcache.CacheException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.squirrel.client.config.zookeeper.CacheCuratorClient;
import com.dianping.squirrel.client.util.DTOUtils;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.util.PathUtils;

/**
 * Remote centralized managed cache item config
 * 
 * @author danson.liu
 * 
 */
public class StoreCategoryConfigManager {

	private static transient Logger logger = LoggerFactory.getLogger(StoreCategoryConfigManager.class);

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();

	private ConcurrentMap<String, CacheKeyType> cacheKeyTypes = new ConcurrentHashMap<String, CacheKeyType>();

	private Set<String> usedCategories = new ConcurrentSkipListSet<String>();

	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static StoreCategoryConfigManager instance;
	
	private List<StoreCategoryConfigListener> configListeners;
    
    private StoreCategoryConfigManager() {
        try {
            init();
        } catch (Exception e) {
            logger.error("failed to init cache item config manager", e);
        }
    }
    
    public static StoreCategoryConfigManager getInstance() {
        if(instance == null) {
            synchronized(StoreClientConfigManager.class) {
                if(instance == null) {
                    instance = new StoreCategoryConfigManager();
                }
            }
        }
        return instance;
    }
	
    public synchronized void addConfigListener(StoreCategoryConfigListener listener) {
        checkNotNull(listener, "category config listener is null");
        if(configListeners == null) {
            configListeners = new ArrayList<StoreCategoryConfigListener>();
        }
        configListeners.add(listener);
    }
    
    private void fireConfigChanged(CacheKeyType categoryConfig) {
        if(configListeners != null) {
            for(StoreCategoryConfigListener listener : configListeners) {
                try {
                    listener.configChanged(categoryConfig);
                } catch(Throwable t) {
                    logger.error("failed to notify category config change: " + categoryConfig, t);
                }
            }
        }
    }
    
    private void fireConfigRemoved(CacheKeyType categoryConfig) {
        if(configListeners != null) {
            for(StoreCategoryConfigListener listener : configListeners) {
                try {
                    listener.configRemoved(categoryConfig);
                } catch(Throwable t) {
                    logger.error("failed to notify category config remove: " + categoryConfig, t);
                }
            }
        }
    }
    
	public CacheKeyType getCacheKeyType(String category) {
		return cacheKeyTypes.get(category);
	}

	public CacheKeyType init(String category) {
		CacheKeyType cacheKeyType = cacheKeyTypes.get(category);
		if (cacheKeyType == null) {
			synchronized (this) {
				cacheKeyType = cacheKeyTypes.get(category);
				if (cacheKeyType == null) {
					CacheKeyConfigurationDTO categoryConfig;
                    try {
                        categoryConfig = loadCategoryConfig(category);
                    } catch (Exception e) {
                        throw new StoreException("failed to load category config: " + category, e);
                    }
					if (categoryConfig == null) {
						logger.error("category config is null: " + category);
						Cat.logError(new CacheException("category config is null: " + category));
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

	public CacheKeyType findCacheKeyType(String category) {
	    if(StringUtils.isBlank(category)) {
	        throw new NullPointerException("store category is empty");
	    }
		if (!usedCategories.contains(category)) {
			usedCategories.add(category);
		}
		return init(category);
	}

	private CacheKeyConfigurationDTO loadCategoryConfig(String category) throws Exception {
	    logger.debug("loading category config from zookeeper: " + category);
	    CacheKeyConfigurationDTO categoryConfig = cacheCuratorClient.getCategoryConfig(category);
	    return categoryConfig;
	}

	/**
	 * @param configurationDTO
	 */
	public void updateConfig(CacheKeyConfigurationDTO configurationDTO) {
	    CacheKeyType categoryConfig = registerCacheKey(configurationDTO);
		fireConfigChanged(categoryConfig);
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

	public Set<String> getCacheItemKeys() {
		return usedCategories;
	}

	public void removeCacheKeyType(String category) {
	    CacheKeyType categoryConfig = cacheKeyTypes.remove(category);
	    if(categoryConfig != null) {
	        fireConfigRemoved(categoryConfig);
	    }
	}

	public void init() throws Exception {
		initCacheCategories();
	}
	
	private void initCacheCategories() {
	    // TODO: enable this switch after all clients are supported
	    if (PathUtils.isZookeeperEnabled() && false) {
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
