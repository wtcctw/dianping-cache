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
package com.dianping.avatar.cache.client;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.jms.CacheCuratorClient;
import com.dianping.cache.builder.CacheClientFactory;
import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.core.CacheClient;
import com.dianping.cache.core.CacheClientBuilder;
import com.dianping.cache.core.CacheClientConfiguration;
import com.dianping.cache.core.CacheConfiguration;
import com.dianping.cache.dcache.DCacheClientImpl;
import com.dianping.cache.exception.InitializingException;
import com.dianping.cache.redis.RedisClientImpl;
import com.dianping.cache.util.ZKUtils;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.remote.cache.CacheConfigurationWebService;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheConfigurationsDTO;

/**
 * Remote centralized managed cache client config
 * 
 * @author danson.liu
 * 
 */
public class RemoteCacheClientFactory implements CacheClientFactory {

	private static transient Logger logger = LoggerFactory.getLogger(RemoteCacheClientFactory.class);

	private ConcurrentMap<String, CacheClientConfiguration> configMap = new ConcurrentHashMap<String, CacheClientConfiguration>();

	private Set<String> usedCacheServices = new ConcurrentSkipListSet<String>();

	private CacheConfigurationWebService configurationWebService;

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();
	
	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static RemoteCacheClientFactory instance;
	
	private RemoteCacheClientFactory() {
	    try {
            init();
        } catch (Exception e) {
            logger.error("failed to init cache client factory", e);
        }
	}
	
	public static RemoteCacheClientFactory getInstance() {
	    if(instance == null) {
	        synchronized(RemoteCacheClientFactory.class) {
	            if(instance == null) {
	                instance = new RemoteCacheClientFactory();
	            }
	        }
	    }
	    return instance;
	}
	
	@Override
	public CacheClient findCacheClient(String cacheKey) {
	    if(StringUtils.isBlank(cacheKey)) {
	        throw new NullPointerException("cache service is empty");
	    }
		if (!usedCacheServices.contains(cacheKey)) {
			usedCacheServices.add(cacheKey);
		}
		return init(cacheKey);
	}

	@Override
	public CacheClient init(String cacheKey) {
		CacheClientConfiguration config = configMap.get(cacheKey);
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

	@Override
	public Set<String> getCacheClientKeys() {
		return usedCacheServices;
	}

	@Override
	public CacheClientConfiguration getCacheClientConfig(String cacheKey) {
		return configMap.get(cacheKey);
	}

	/**
	 * poll configuration from remote cache server
	 * 
	 * @throws InitializingException
	 */
	private void pollConfigurationFromServer() throws InitializingException {
		try {
			configMap.clear();
			CacheConfigurationsDTO configurations = configurationWebService.getCacheConfigurations();
			for (String key : configurations.keys()) {
				registerCache(configurations.getConfiguration(key));
			}
		} catch (InitializingException e) {
			logger.error("", e);
			throw e;
		} catch (Exception e) {
			String errorMsg = "Poll cache configuration from cache server failed.";
			logger.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	public void updateCache(CacheConfigurationDTO configurationDTO) throws InitializingException {
		registerCache(configurationDTO);
	}

	/**
	 * @param key
	 * @param configuration
	 * @throws InitializingException
	 */
	private synchronized CacheClientConfiguration registerCache(CacheConfigurationDTO configuration) throws InitializingException {
		logger.warn("register cache service: " + configuration);
		String cacheKey = configuration.getCacheKey();

		if (cacheKey.startsWith("dcache")) {
		    configuration.setClientClazz(DCacheClientImpl.class.getName());
		    configuration.setTranscoderClazz(com.dianping.cache.dcache.HessianTranscoder.class.getName());
		}
		if (cacheKey.startsWith("redis")) {
            configuration.setClientClazz(RedisClientImpl.class.getName());
        }
		// deprecated
		if ("com.dianping.cache.memcached.HessianTranscoder".equals(configuration.getTranscoderClazz())) {
		    String classLocal = null;
		    String transcoderClassLocal = null;
		    String classKey = "avatar-cache.class$" + configuration.getClientClazz();
		    String transcoderClassKey = "avatar-cache.transcoderclass$" + configuration.getTranscoderClazz();
		    
		    classLocal = ConfigManagerLoader.getConfigManager().getStringValue(classKey);
		    transcoderClassLocal = ConfigManagerLoader.getConfigManager().getStringValue(transcoderClassKey);
		    
		    if (StringUtils.isNotBlank(classLocal)) {
		        configuration.setClientClazz(classLocal);
		    }
		    if (StringUtils.isNotBlank(transcoderClassLocal)) {
		        configuration.setTranscoderClazz(transcoderClassLocal);
		    }
		}
		
		CacheClientConfiguration cacheClientConfig = CacheClientConfigurationHelper.parse(configuration);
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
        if (ZKUtils.isZookeeperEnabled()) {
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
