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
package com.dianping.squirrel.client.impl.memcached;

import java.util.List;

import org.apache.commons.lang.ClassUtils;

import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.config.StoreClientConfigParser;
import com.dianping.squirrel.client.config.zookeeper.CacheMessageManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.domain.CacheConfigurationDTO;

/**
 * TODO Comment of MemcachedClientConfigurationParser
 * 
 * @author danson.liu
 * 
 */
public class MemcachedClientConfigParser implements StoreClientConfigParser {

	@Override
	public StoreClientConfig parse(CacheConfigurationDTO detail) {
		MemcachedClientConfig config = new MemcachedClientConfig();
		config.setCacheConfigurationListener(CacheMessageManager.getInstance());
		String transcoderClass = ConfigManagerLoader.getConfigManager().getStringValue(
		        "avatar-cache.memcached.transcoder.class", "com.dianping.squirrel.client.impl.memcached.MemcachedTranscoder");

		if (transcoderClass != null && !transcoderClass.trim().isEmpty()) {
			try {
				Class<?> cz = ClassUtils.getClass(transcoderClass.trim());
				config.setTranscoderClass(cz);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to set memcached's transcoder.", ex);
			}
		}

		List<String> serverList = detail.getServerList();
		if (serverList == null || serverList.size() == 0) {
			throw new RuntimeException("Memcached config's server list must not be empty.");
		}
		config.setServerList(serverList);

		config.setClientClazz(detail.getClientClazz());

		return config;
	}

}
