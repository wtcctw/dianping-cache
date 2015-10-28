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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.impl.dcache.DCacheClientConfigParser;
import com.dianping.squirrel.client.impl.dcache.DCacheStoreClientImpl;
import com.dianping.squirrel.client.impl.ehcache.EhcacheClientConfigParser;
import com.dianping.squirrel.client.impl.ehcache.EhcacheStoreClientImpl;
import com.dianping.squirrel.client.impl.memcached.MemcachedClientConfigParser;
import com.dianping.squirrel.client.impl.memcached.MemcachedClientImpl;
import com.dianping.squirrel.client.impl.redis.RedisClientConfigParser;
import com.dianping.squirrel.client.impl.redis.RedisStoreClientImpl;
import com.dianping.squirrel.common.exception.StoreInitializeException;

/**
 * CacheClientConfiguration parse helper class
 * 
 * @author danson.liu
 * 
 */
public class StoreClientConfigHelper {
	private static transient Logger logger = LoggerFactory.getLogger(StoreClientConfigHelper.class);
	private static Map<Class, StoreClientConfigParser> parserMap = new ConcurrentHashMap<Class, StoreClientConfigParser>();

	static {
		register(MemcachedClientImpl.class, new MemcachedClientConfigParser());
		register(EhcacheStoreClientImpl.class, new EhcacheClientConfigParser());
		register(DCacheStoreClientImpl.class, new DCacheClientConfigParser());
		register(RedisStoreClientImpl.class, new RedisClientConfigParser());
	}

	public static void register(Class clientClazz, StoreClientConfigParser parser) {
		parserMap.put(clientClazz, parser);
	}

	/**
	 * @param detail
	 * @return
	 */
	public static StoreClientConfig parse(CacheConfigurationDTO detail) throws StoreInitializeException {
		try {
			Class clientClazz = Class.forName(detail.getClientClazz());
			StoreClientConfig config = parserMap.get(clientClazz).parse(detail);
			config.init();
			return config;
		} catch (ClassNotFoundException e) {
			logger.error("Parser not found with cache client[" + detail.getClientClazz() + "].", e);
			return null;
		} catch (NoClassDefFoundError e) {
			logger.error("Parser not found with cache client[" + detail.getClientClazz() + "].", e);
			return null;
		} catch (RuntimeException e) {
			throw new StoreInitializeException("Error while parsing config[" + detail.getClientClazz() + "].", e);
		}
	}

}