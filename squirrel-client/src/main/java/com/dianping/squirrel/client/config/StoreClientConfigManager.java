/**
 * Project: avatar
 * 
 * File Created at 2010-10-18
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.remote.cache.CacheConfigurationWebService;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheConfigurationsDTO;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.config.zookeeper.CacheCuratorClient;
import com.dianping.squirrel.client.core.CacheClientBuilder;
import com.dianping.squirrel.client.core.CacheConfiguration;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.impl.dcache.DCacheStoreClientImpl;
import com.dianping.squirrel.client.impl.dcache.DCacheTranscoder;
import com.dianping.squirrel.client.impl.memcached.MemcachedStoreClientImpl;
import com.dianping.squirrel.client.impl.redis.RedisStoreClientImpl;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreInitializeException;
import com.dianping.squirrel.common.util.PathUtils;

/**
 * Remote centralized managed cache client config
 * 
 * @author danson.liu
 * 
 */
public class StoreClientConfigManager {

	private static transient Logger logger = LoggerFactory.getLogger(StoreClientConfigManager.class);

	private ConcurrentMap<String, StoreClientConfig> configMap = new ConcurrentHashMap<String, StoreClientConfig>();

	private Set<String> usedCacheServices = new ConcurrentSkipListSet<String>();

	private CacheConfigurationWebService configurationWebService;

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();
	
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static StoreClientConfigManager instance;
	
	private StoreClientConfigManager() {
	    try {
            init();
        } catch (Exception e) {
            logger.error("failed to init cache client factory", e);
        }
	}
	
	public static StoreClientConfigManager getInstance() {
	    if(instance == null) {
	        synchronized(StoreClientConfigManager.class) {
	            if(instance == null) {
	                instance = new StoreClientConfigManager();
	            }
	        }
	    }
	    return instance;
	}
	
	public StoreClient findCacheClient(String cacheKey) {
	    if(StringUtils.isBlank(cacheKey)) {
	        throw new NullPointerException("cache service is empty");
	    }
		if (!usedCacheServices.contains(cacheKey)) {
			usedCacheServices.add(cacheKey);
		}
		return init(cacheKey);
	}

	public StoreClient init(String cacheKey) {
		StoreClientConfig config = configMap.get(cacheKey);
		if (config == null) {
			synchronized (this) {
				config = configMap.get(cacheKey);
				if (config == null) {
					CacheConfigurationDTO serviceConfig = loadCacheClientConfig(cacheKey);
					if (serviceConfig == null) {
						throw new IllegalArgumentException("failed to get cache service config: " + cacheKey);
					}
					logger.info("loaded cache service config: " + serviceConfig);
					config = registerCache(serviceConfig);
				}
			}
		}
		if (config != null) {
			return CacheClientBuilder.buildCacheClient(cacheKey, config);
		}
		return null;
	}

	private CacheConfigurationDTO loadCacheClientConfig(String cacheKey) {
		CacheConfigurationDTO serviceConfig = null;
		try {
			serviceConfig = cacheCuratorClient.getServiceConfig(cacheKey);
		} catch (Exception e) {
			logger.error("failed to get cache service config from zookeeper: " + cacheKey, e);
		}
		if (serviceConfig == null) {
			serviceConfig = configurationWebService.getCacheConfiguration(cacheKey);
		}
		return serviceConfig;
	}

	public Set<String> getCacheClientKeys() {
		return usedCacheServices;
	}

	public StoreClientConfig getCacheClientConfig(String cacheKey) {
		return configMap.get(cacheKey);
	}

	/**
	 * poll configuration from remote cache server
	 * 
	 * @throws StoreInitializeException
	 */
	private void pollConfigurationFromServer() throws StoreInitializeException {
		try {
			configMap.clear();
			CacheConfigurationsDTO configurations = configurationWebService.getCacheConfigurations();
			for (String key : configurations.keys()) {
				registerCache(configurations.getConfiguration(key));
			}
		} catch (StoreInitializeException e) {
			logger.error("", e);
			throw e;
		} catch (Exception e) {
			String errorMsg = "Poll cache configuration from cache server failed.";
			logger.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	public void updateCache(CacheConfigurationDTO configurationDTO) throws StoreInitializeException {
		registerCache(configurationDTO);
	}

	/**
	 * @param key
	 * @param configuration
	 * @throws StoreInitializeException
	 */
	private synchronized StoreClientConfig registerCache(CacheConfigurationDTO configuration) throws StoreInitializeException {
		logger.warn("register cache service: " + configuration);
		String cacheKey = configuration.getCacheKey();

		if(cacheKey.startsWith("memcache")) {
		    configuration.setClientClazz(MemcachedStoreClientImpl.class.getName());
		} else if (cacheKey.startsWith("dcache")) {
		    configuration.setClientClazz(DCacheStoreClientImpl.class.getName());
		    configuration.setTranscoderClazz(DCacheTranscoder.class.getName());
		} else if (cacheKey.startsWith("redis")) {
            configuration.setClientClazz(RedisStoreClientImpl.class.getName());
        }
		
		StoreClientConfig cacheClientConfig = StoreClientConfigHelper.parse(configuration);
		if (cacheClientConfig != null) {
		    configMap.put(cacheKey, cacheClientConfig);
		}
		
		CacheConfiguration.removeCache(cacheKey);
		CacheConfiguration.addCache(cacheKey, configuration.getClientClazz());
		CacheClientBuilder.closeCacheClient(cacheKey);

		return cacheClientConfig;
	}

	public void init() throws Exception {
	    configurationWebService = ServiceFactory.getService(
                "http://service.dianping.com/cacheService/cacheConfigService_1.0.0",
                CacheConfigurationWebService.class, 10000);
		initCacheServices();
	}
	
	private void initCacheServices() {
	    // TODO open the switch after all clients are supported
        if (PathUtils.isZookeeperEnabled() && false) {
            String appName = configManager.getAppName();
            if (StringUtils.isNotEmpty(appName)) {
                try {
                    String services = cacheCuratorClient.getRuntimeServices(appName);
                    if (StringUtils.isNotEmpty(services)) {
                        logger.info("initializing cache services: " + services);
                        String[] cacheServices = StringUtils.split(services, ',');
                        for (String cacheService : cacheServices) {
                            if(StringUtils.isNotBlank(cacheService)) {
                                init(cacheService.trim());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("failed to initialize cache services", e);
                }
            }
        }
	}

}
