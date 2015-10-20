/**
 * Project: avatar-cache
 * 
 * File Created at 2011-9-13
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
package com.dianping.avatar.cache.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManagerLoader;

/**
 * 
 * @author danson.liu
 */
public class CacheMonitorUtil {

	private static final Logger logger = LoggerFactory.getLogger(CacheMonitorUtil.class);

	private static ConcurrentMap<String, Integer> logFactorMap = new ConcurrentHashMap<String, Integer>();

	private static final int logInterval = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.log.interval", 100);

	/**
	 * 记录指定category未配置缓存项配置
	 * 
	 * @param category
	 */
	public static void logConfigNotFound(String category, String error, int logInterval) {
		int logFactorNew = getNewLogFactor(category);
		if (logFactorNew % logInterval == 0) {
			logger.error(error);
		}
	}

	/**
	 * 记录缓存访问错误日志，大部分报到本地日志文件，小部分手机到hawk
	 * 
	 * @param errorMsg
	 * @param throwable
	 */
	public static void logCacheError(String errorMsg, Throwable throwable) {
		String factorName = StringUtils.substringBefore(errorMsg, "[");
		int logFactorNew = getNewLogFactor(factorName);
		if (logFactorNew % logInterval == 0) {
			logger.error("Operate cache error: " + errorMsg, throwable);
		}
	}

	private static int getNewLogFactor(String factorName) {
		Integer newFactor = null;
		if (logFactorMap.containsKey(factorName)) {
			Integer oldFactor = logFactorMap.get(factorName);
			newFactor = oldFactor + 1;
			logFactorMap.put(factorName, newFactor);
		} else {
			logFactorMap.put(factorName, 0);
			newFactor = 0;
		}
		return newFactor;
	}

}
