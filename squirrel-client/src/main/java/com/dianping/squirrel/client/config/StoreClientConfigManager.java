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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.config.zookeeper.CacheCuratorClient;
import com.dianping.squirrel.client.core.CacheConfiguration;
import com.dianping.squirrel.client.core.StoreClientBuilder;
import com.dianping.squirrel.client.impl.danga.DangaStoreClientImpl;
import com.dianping.squirrel.client.impl.dcache.DCacheStoreClientImpl;
import com.dianping.squirrel.client.impl.ehcache.EhcacheStoreClientImpl;
import com.dianping.squirrel.client.impl.memcached.MemcachedStoreClientImpl;
import com.dianping.squirrel.client.impl.redis.RedisStoreClientImpl;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
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

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();
	
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static StoreClientConfigManager instance;
	
	private Map<String, List<StoreClientConfigListener>> configListenerMap = new HashMap<String, List<StoreClientConfigListener>>();
	
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
	
	public void addConfigListener(String storeType, StoreClientConfigListener listener) {
	    checkNotNull(storeType, "store type is null");
	    checkNotNull(listener, "client config listener is null");
	    synchronized(configListenerMap) {
	        List<StoreClientConfigListener> listeners = configListenerMap.get(storeType);
	        if(listeners == null) {
	            listeners = new ArrayList<StoreClientConfigListener>();
	            configListenerMap.put(storeType, listeners);
	        }
	        listeners.add(listener);
	    }
	}
	
	private void fireConfigChanged(String storeType, StoreClientConfig clientConfig) {
	    synchronized(configListenerMap) {
	        List<StoreClientConfigListener> listeners = configListenerMap.get(storeType);
            if(listeners != null) {
                for(StoreClientConfigListener listener : listeners) {
                    try {
                        listener.configChanged(clientConfig);
                    } catch(Throwable t) {
                        logger.error("failed to notify client config change: " + clientConfig, t);
                    }
                }
            }
	    }
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

	public StoreClient init(String cacheKey) throws StoreException {
		StoreClientConfig clientConfig = configMap.get(cacheKey);
		if (clientConfig == null) {
			synchronized (this) {
			    clientConfig = configMap.get(cacheKey);
				if (clientConfig == null) {
					CacheConfigurationDTO configDto;
                    try {
                        configDto = loadCacheClientConfig(cacheKey);
                    } catch (Exception e) {
                        throw new StoreException("failed to load store client config: " + cacheKey, e);
                    }
                    if (configDto == null) {
                        throw new StoreException("store client config is null: " + cacheKey);
                    }
                    clientConfig = parse(configDto);
                    if(clientConfig != null) {
                        configMap.put(cacheKey, clientConfig);
                    }
				}
			}
		}
		if(clientConfig != null) {
		    return StoreClientBuilder.buildStoreClient(cacheKey, clientConfig);
		}
		return null;
	}

	private CacheConfigurationDTO loadCacheClientConfig(String cacheKey) throws Exception {
	    CacheConfigurationDTO serviceConfig = cacheCuratorClient.getServiceConfig(cacheKey);
	    return serviceConfig;
	}

	public Set<String> getCacheClientKeys() {
		return usedCacheServices;
	}

	public StoreClientConfig getCacheClientConfig(String cacheKey) {
		return configMap.get(cacheKey);
	}

	public void updateCache(CacheConfigurationDTO configDto) throws StoreInitializeException {
//		registerCache(configurationDTO);
	    checkNotNull(configDto, "store config dto is null");
	    StoreClientConfig clientConfig = parse(configDto);
	    if(clientConfig != null) {
	        configMap.put(configDto.getCacheKey(), clientConfig);
	        fireConfigChanged(configDto.getCacheKey(), clientConfig);
	    }
	}
	
	private StoreClientConfig parse(CacheConfigurationDTO configDto) {
	    String storeType = configDto.getCacheKey();

        if(storeType.startsWith("memcache")) {
            configDto.setClientClazz(MemcachedStoreClientImpl.class.getName());
        } else if (storeType.startsWith("dcache")) {
            configDto.setClientClazz(DCacheStoreClientImpl.class.getName());
        } else if (storeType.startsWith("redis")) {
            configDto.setClientClazz(RedisStoreClientImpl.class.getName());
        } else if(storeType.startsWith("web") || storeType.startsWith("ehcache")) {
            configDto.setClientClazz(EhcacheStoreClientImpl.class.getName());
        } else if(storeType.startsWith("danga")){
        	configDto.setClientClazz(DangaStoreClientImpl.class.getName());
        }
        
        StoreClientConfig clientConfig = StoreClientConfigHelper.parse(configDto);
        return clientConfig;
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
		} else if (cacheKey.startsWith("redis")) {
            configuration.setClientClazz(RedisStoreClientImpl.class.getName());
        } else if(cacheKey.startsWith("web") || cacheKey.startsWith("ehcache")) {
            configuration.setClientClazz(EhcacheStoreClientImpl.class.getName());
        }
		
		StoreClientConfig cacheClientConfig = StoreClientConfigHelper.parse(configuration);
		if (cacheClientConfig != null) {
		    configMap.put(cacheKey, cacheClientConfig);
		}
		
		CacheConfiguration.removeCache(cacheKey);
		CacheConfiguration.addCache(cacheKey, configuration.getClientClazz());
		StoreClientBuilder.closeStoreClient(cacheKey);

		return cacheClientConfig;
	}

	public void init() throws Exception {
		initCacheServices();
	}
	
	private void initCacheServices() {
	    // TODO test
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
