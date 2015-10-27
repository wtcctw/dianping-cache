/**
 * Project: com.dianping.avatar-cache-2.0.0-SNAPSHOT
 * 
 * File Created at 2011-2-20
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

import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;

/**
 * Cache Key Configuration Update Listener
 * 
 * @author danson.liu
 * 
 */
public class CacheKeyConfigUpdateListener {

	private final Logger logger = LoggerFactory.getLogger(CacheKeyConfigUpdateListener.class);

	public void handleMessage(CacheKeyConfigurationDTO configurationDTO) {
		if (configurationDTO != null) {
			try {
			    if(StoreCategoryConfigManager.getInstance().getCacheKeyType(configurationDTO.getCategory()) != null) {
			        if(configurationDTO.getVersion() == -1) {
			            // category is removed
			            StoreCategoryConfigManager.getInstance().removeCacheKeyType(configurationDTO.getCategory());
			            logger.warn("cache category [" + configurationDTO.getCategory() + "] is removed");
			        } else {
			            StoreCategoryConfigManager.getInstance().updateConfig(configurationDTO);
			            logger.warn("cache category config upadted to: " + configurationDTO);
			        }
			    } else {
			        logger.error("cache category config not found: " + configurationDTO);
			    }
			} catch (Throwable e) {
				logger.error("failed to update cache category config to: " + configurationDTO, e);
			}
		}
	}

}
