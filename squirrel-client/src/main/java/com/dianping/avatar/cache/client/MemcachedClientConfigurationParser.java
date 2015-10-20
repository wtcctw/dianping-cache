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

import java.util.List;

import org.apache.commons.lang.ClassUtils;

import com.dianping.avatar.cache.jms.CacheMessageManager;
import com.dianping.cache.core.CacheClientConfiguration;
import com.dianping.cache.memcached.MemcachedClientConfiguration;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;

/**
 * TODO Comment of MemcachedClientConfigurationParser
 * 
 * @author danson.liu
 * 
 */
public class MemcachedClientConfigurationParser implements CacheClientConfigurationParser {

	@Override
	public CacheClientConfiguration parse(CacheConfigurationDTO detail) {
		MemcachedClientConfiguration config = new MemcachedClientConfiguration();
		config.setCacheConfigurationListener(CacheMessageManager.getInstance());
		String transcoderClass = ConfigManagerLoader.getConfigManager().getStringValue(
		        "avatar-cache.memcached.transcoder.class", detail.getTranscoderClazz());

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
