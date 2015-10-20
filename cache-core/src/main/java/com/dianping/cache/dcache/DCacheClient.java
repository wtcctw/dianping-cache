/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.cache.dcache;

import com.dianping.cache.core.CacheClient;
import com.dianping.cache.exception.CacheUnavailableException;
import com.qq.cloud.component.dcache.client.api.DCacheClientAPI;
import com.qq.cloud.component.dcache.client.kv.DCacheKVClientAPI;

public interface DCacheClient extends CacheClient {

	public DCacheClientAPI getClient() throws CacheUnavailableException;
	
	public DCacheKVClientAPI getKVClient() throws CacheUnavailableException;
}
