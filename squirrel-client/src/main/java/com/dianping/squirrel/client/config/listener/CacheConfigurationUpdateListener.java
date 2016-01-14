/**
 * Project: com.dianping.avatar-cache-2.0.0-SNAPSHOT
 * 
 * File Created at 2011-2-19
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.squirrel.client.config.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.common.domain.CacheConfigurationDTO;

/**
 * Cache Configuration Update Listener
 * 
 * @author danson.liu
 * 
 */
public class CacheConfigurationUpdateListener {

	private final Logger logger = LoggerFactory.getLogger(CacheConfigurationUpdateListener.class);

	public void handleMessage(CacheConfigurationDTO configurationDTO) {
		if (configurationDTO != null) {
			try {
			    if(StoreClientConfigManager.getInstance().getCacheClientConfig(configurationDTO.getCacheKey()) != null) {
			        StoreClientConfigManager.getInstance().updateCache(configurationDTO);
					logger.warn("cache service config updated to: " + configurationDTO);
			    } else {
			        logger.error("cache service config not found: " + configurationDTO);
			    }
			} catch (Throwable e) {
				logger.error("failed to update cache service config to: " + configurationDTO, e);
			}
		}
	}

}
