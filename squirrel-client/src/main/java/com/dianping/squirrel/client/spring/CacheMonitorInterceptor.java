/**
 * Project: avatar-cache
 * 
 * File Created at 2011-9-19
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
package com.dianping.squirrel.client.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.dianping.squirrel.client.config.CacheItemConfigManager;
import com.dianping.squirrel.client.config.EnhancedCacheItemConfigManager;

/**
 * dedicates to statistics the cache hit-rate, the corresponding result could be shown on the hawk-console.
 * <br>Statistics category:
 * <ul>
 * 		<li>命中率：打点格式(k1: cache，k2: hit, k3: category, k4: get/mGet, value: 0/1---0: 未命中，1: 命中)
        <li>操作耗时：打点格式(k1: cache, k2: cost, k3: category, k4: get/add/remove, value: 时间[ms])
        <li>调用次数：打点格式(k1: cache, k2: count, k3: category, k4: get/add/remove, value: 1)
   <ul>
 * @author youngphy.yang
 *
 */
public class CacheMonitorInterceptor implements MethodInterceptor{
	
	private CacheItemConfigManager cacheItemConfigManager = null;
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = invocation.proceed();
		return result;
	}
	
	public CacheItemConfigManager getCacheItemConfigManager() {
		return cacheItemConfigManager;
	}

	public void setCacheItemConfigManager(
			CacheItemConfigManager cacheItemConfigManager) {
		this.cacheItemConfigManager = new EnhancedCacheItemConfigManager(cacheItemConfigManager);
	}
	
}