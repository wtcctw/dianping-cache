/**
 * Project: avatar-cache
 * 
 * File Created at 2010-7-12
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
package com.dianping.squirrel.client.core;

import static com.google.common.base.Preconditions.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.config.StoreClientConfigListener;
import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.common.exception.StoreException;

/**
 * Build cache client from configuration file. Each cache key will be built only
 * one instance. And, if the client implements {@link Lifecycle}, it will be
 * started when first built, and will be shutdown when invoking
 * {@link #closeStoreClient(String)}.
 * 
 * @author guoqing.chen
 * @author enlight.chen
 */
public class StoreClientBuilder {
    
    private static Logger logger = LoggerFactory.getLogger(StoreClientBuilder.class);
    
	/**
	 * The caches for all client implementation
	 */
	private static Map<String, StoreClient> clientMap = new ConcurrentHashMap<String, StoreClient>();

	public static StoreClient getStoreClient(String storeType, StoreClientConfig config) {
		checkNotNull(storeType, "store type is null");
		StoreClient storeClient = clientMap.get(storeType);
		if (storeClient != null) {
			return storeClient;
		} else {
			return buildStoreClient(storeType, config);
		}
	}

	/**
	 * Build a cache client by configuration file. The client will be started if
	 * it implements {@link Lifecycle} interface. The client instance will be
	 * cached to HashMap for multiple retrieves. Every key will only be built
	 * for one instance.
	 */
	public synchronized static StoreClient buildStoreClient(String storeType, StoreClientConfig config) {
	    checkNotNull(storeType, "store type is null");

		StoreClient storeClient = clientMap.get(storeType);
		if (storeClient != null) {
			return storeClient;
		}

		String clientClass = config.getClientClazz();
		checkNotNull(clientClass, "%s's store client class is null", storeType);

		Class<?> cz = null;
		try {
			cz = Thread.currentThread().getContextClassLoader().loadClass(clientClass);
		} catch (ClassNotFoundException e) {
		}

		if (cz == null) {
			try {
				cz = StoreClientBuilder.class.getClassLoader().loadClass(clientClass);
			} catch (ClassNotFoundException e) {
				throw new StoreException("store client class [" + clientClass + "] is not found");
			}
		}

		if (!StoreClient.class.isAssignableFrom(cz)) {
			throw new StoreException("store client class [" + clientClass
					+ "] is not derived from " + StoreClient.class.getName());
		}

		try {
			storeClient = (StoreClient) cz.newInstance();
		} catch (InstantiationException e) {
			throw new StoreException("can not instantiate store client class [" + clientClass + "]", e);
		} catch (IllegalAccessException e) {
			throw new StoreException(e);
		}

		if (storeClient instanceof StoreTypeAware) {
			((StoreTypeAware) storeClient).setStoreType(storeType);
		}
		
		if(storeClient instanceof StoreClientConfigListener) {
		    StoreClientConfigManager.getInstance().addConfigListener(storeType, 
		            ((StoreClientConfigListener)storeClient));
		}

		if (storeClient instanceof Lifecycle) {
		    ((Lifecycle) storeClient).initialize(config);
			((Lifecycle) storeClient).start();
		}

		clientMap.put(storeType, storeClient);
		return storeClient;
	}

	/**
	 * Close the store client by store type.
	 */
	public synchronized static void closeStoreClient(String storeType) {
		if (storeType == null) {
			return;
		}

		StoreClient storeClient = clientMap.get(storeType);
		if (storeClient == null) {
			return;
		}

		if (storeClient instanceof Lifecycle) {
			((Lifecycle) storeClient).stop();
		}

		clientMap.remove(storeType);
	}
	
}
