/**
 * Project: avatar
 * 
 * File Created at 2010-11-1
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
package com.dianping.squirrel.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.tracker.CacheExecutionTrace;
import com.dianping.avatar.tracker.ExecutionContextHolder;
import com.dianping.avatar.tracker.TrackerContext;


/**
 * Default Cache Profiler implementation
 * @author danson.liu
 *
 */
public class DefaultCacheTracker implements CacheTracker {
	
	private static Logger logger = LoggerFactory.getLogger(DefaultCacheTracker.class);
	
	public void addGetInfo(String cacheDesc, long timeConsumed) {
		try {
			if (ExecutionContextHolder.isTrackRequired()) {
				TrackerContext trackerContext = ExecutionContextHolder.getTrackerContext();
				CacheExecutionTrace cacheExecutionTrace = trackerContext.getCacheExecutionTrace();
				cacheExecutionTrace.addTimeConsumed(timeConsumed);
				cacheExecutionTrace.addRelatedKey(cacheDesc);
			}
		} catch (Exception e) {
			logger.warn("Failed to track cache execution.", e);
		}
	}

}
