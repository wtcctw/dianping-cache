/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.squirrel.client.impl.dcache;

import com.dianping.squirrel.client.core.CacheClient;
import com.qq.cloud.component.dcache.client.api.DCacheClientAPI;
import com.qq.cloud.component.dcache.client.kv.DCacheKVClientAPI;

public interface DCacheClient extends CacheClient {

	public DCacheClientAPI getClient();
	
	public DCacheKVClientAPI getKVClient();
	
}
