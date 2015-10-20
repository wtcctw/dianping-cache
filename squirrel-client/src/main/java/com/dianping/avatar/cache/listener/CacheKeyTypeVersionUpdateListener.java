/**
 * Project: avatar
 * 
 * File Created at 2010-10-15
 * $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.avatar.cache.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.configuration.CacheKeyType;
import com.dianping.avatar.cache.configuration.RemoteCacheItemConfigManager;
import com.dianping.avatar.cache.util.IPUtils;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;

/**
 * CacheKeyTypeVersionUpdateListener is used to listen message that to Update
 * version for some key type.
 * 
 * @author pengshan.zhang
 * 
 */
public class CacheKeyTypeVersionUpdateListener {

	private final Logger logger = LoggerFactory.getLogger(CacheKeyTypeVersionUpdateListener.class);

	private String serverIp;

	public void handleMessage(CacheKeyTypeVersionUpdateDTO versionUpdateDTO) {
		if (versionUpdateDTO != null) {
			List<String> destinations = versionUpdateDTO.getDestinations();
			if(serverIp == null) {
			    serverIp = IPUtils.getFirstNoLoopbackIP4Address();
			}
			if (destinations == null || destinations.contains(serverIp)) {
				String category = versionUpdateDTO.getMsgValue();
				String version = versionUpdateDTO.getVersion();
				int versionInt = 0;
				try {
					versionInt = Integer.parseInt(version);
				} catch (Exception e) {
					logger.error(String.format("cache category %s invalid version %s", category, version));
					return;
				}
				CacheKeyType keyType = RemoteCacheItemConfigManager.getInstance().getCacheKeyType(category);
				if (keyType != null) {
					keyType.setVersion(versionInt);
					logger.info(String.format("cache category %s version changed to %s", category, version));
				} else {
					logger.error("cache category [" + category + "] not found");
				}
			}
		}
	}

}
