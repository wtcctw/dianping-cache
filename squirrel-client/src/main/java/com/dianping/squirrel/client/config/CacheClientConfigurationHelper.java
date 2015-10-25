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
import com.dianping.squirrel.client.config.parser.CacheClientConfigurationParser;
import com.dianping.squirrel.client.config.parser.DCacheClientConfigurationParser;
import com.dianping.squirrel.client.config.parser.EhcacheClientConfigurationParser;
import com.dianping.squirrel.client.config.parser.MemcachedClientConfigurationParser;
import com.dianping.squirrel.client.config.parser.RedisClusterClientConfigurationParser;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.core.CacheClientConfiguration;
import com.dianping.squirrel.client.impl.dcache.DCacheClientImpl;
import com.dianping.squirrel.client.impl.ehcache.EhcacheClientImpl;
import com.dianping.squirrel.client.impl.memcached.MemcachedClientImpl;
import com.dianping.squirrel.client.impl.redis.RedisStoreClientImpl;
import com.dianping.squirrel.common.exception.StoreInitializeException;

/**
 * CacheClientConfiguration parse helper class
 * 
 * @author danson.liu
 * 
 */
public class CacheClientConfigurationHelper {
	private static transient Logger logger = LoggerFactory.getLogger(CacheClientConfigurationHelper.class);
	private static Map<Class<? extends CacheClient>, CacheClientConfigurationParser> parserMap = new ConcurrentHashMap<Class<? extends CacheClient>, CacheClientConfigurationParser>();

	static {
		register(MemcachedClientImpl.class, new MemcachedClientConfigurationParser());
		register(EhcacheClientImpl.class, new EhcacheClientConfigurationParser());
		register(DCacheClientImpl.class, new DCacheClientConfigurationParser());
		register(RedisStoreClientImpl.class, new RedisClusterClientConfigurationParser());
	}

	public static void register(Class<? extends CacheClient> clientClazz, CacheClientConfigurationParser parser) {
		parserMap.put(clientClazz, parser);
	}

	/**
	 * @param detail
	 * @return
	 */
	public static CacheClientConfiguration parse(CacheConfigurationDTO detail) throws StoreInitializeException {
		try {
			Class clientClazz = Class.forName(detail.getClientClazz());
			CacheClientConfiguration config = parserMap.get(clientClazz).parse(detail);
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