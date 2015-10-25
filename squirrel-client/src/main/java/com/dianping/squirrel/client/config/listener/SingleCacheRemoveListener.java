/**
 * Project: avatar
 * 
 * File Created at 2010-10-15 $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.squirrel.client.config.listener;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;
import com.dianping.squirrel.client.config.RemoteCacheClientFactory;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.util.IPUtils;
import com.dianping.squirrel.common.util.ZKUtils;

/**
 * LocalCacheRemoveListener is used to remove local cache after receiving
 * removing local cache message.
 * 
 * @author pengshan.zhang
 * @author danson.liu
 * 
 */
public class SingleCacheRemoveListener {

	/**
	 * Logger instance.
	 */
	private final Logger logger = LoggerFactory.getLogger(SingleCacheRemoveListener.class);

	private static final String CACHE_FINAL_KEY_SEP = "@|$";

	private String serverIp;

	public void handleMessage(SingleCacheRemoveDTO cacheRemoveDTO) {
		if (cacheRemoveDTO != null) {
			List<String> destinations = cacheRemoveDTO.getDestinations();
			if (serverIp == null) {
				serverIp = IPUtils.getFirstNoLoopbackIP4Address();
			}
			if (destinations == null || destinations.contains(serverIp)) {
				String cacheType = cacheRemoveDTO.getCacheType();
				String cacheKeys = cacheRemoveDTO.getCacheKey();
				CacheClient cacheClient = RemoteCacheClientFactory.getInstance().findCacheClient(cacheType);
				if (cacheClient != null) {
					String[] keyList = StringUtils.splitByWholeSeparator(cacheKeys, CACHE_FINAL_KEY_SEP);
					if (keyList != null) {
						List<String> failedKeys = new ArrayList<String>();
						List<String> removedKeys = new ArrayList<String>();
						Throwable lastError = null;
						for (String finalKey : keyList) {
							try {
								cacheClient.asyncDelete(finalKey, true, ZKUtils.getCategoryFromKey(finalKey));
								removedKeys.add(finalKey);
							} catch (Throwable e) {
								failedKeys.add(finalKey);
								lastError = e;
							}
						}
						if (!failedKeys.isEmpty()) {
							logger.warn(String.format("failed to clear cache key %s: failed keys %s, error %s",
									cacheKeys, StringUtils.join(failedKeys, ','), lastError));
						} else if (!removedKeys.isEmpty()) {
							logger.warn(String.format("cleard cache key %s: removed keys %s", cacheKeys,
									StringUtils.join(removedKeys, ',')));
						}
					}
				} else {
					logger.error("failed to clear cache key [" + cacheKeys + "]: no cache client found");
				}
			}
		}
	}

}
