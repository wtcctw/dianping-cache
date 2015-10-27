/**
 * Project: cache-core
 * 
 * File Created at 2010-9-2 $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.squirrel.client.impl.ehcache;

import java.lang.reflect.Field;
import java.net.URL;

import net.sf.ehcache.CacheManager;

import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.common.exception.StoreInitializeException;

/**
 * The configuration for ehcache client implementation
 * 
 * @author pengshan.zhang
 * 
 */
public class EhcacheConfiguration implements StoreClientConfig {

	/**
	 * Default ehcache configuration file
	 */
	private static final String EHCACHE_FILE_URL = "/ehcache.xml";

	private static final String CUSTOM_EHCACHE_FILE_URL = "/config/ehcache-cust.xml";

	private String xmlFile = EHCACHE_FILE_URL;

    private String clientClazz;

    @Override
    public void setClientClazz(String clientClazz) {
        this.clientClazz = clientClazz;
    }

    @Override
    public String getClientClazz() {
        return this.clientClazz;
    }
    
	/**
	 * @return the xmlFile
	 */
	public String getXmlFile() {
		return xmlFile;
	}

	/**
	 * @param xmlFile
	 *            the xmlFile to set
	 */
	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	/**
	 * Create ehcache CacheManager instance from configuration file
	 */
	public CacheManager buildEhcacheManager() {
		if (getClass().getResource(CUSTOM_EHCACHE_FILE_URL) != null) {
			xmlFile = CUSTOM_EHCACHE_FILE_URL;
		}
		URL url = getClass().getResource(xmlFile);
		return CacheManager.create(url);
	}

	@Override
	public void init() throws StoreInitializeException {
		ensureEhcacheVersion();
	}

	private void ensureEhcacheVersion() throws StoreInitializeException {
		try {
			Class clazz = Class.forName("net.sf.ehcache.constructs.EhcacheDecoratorAdapter");
			Field field = clazz.getDeclaredField("underlyingCache");
		} catch (ClassNotFoundException e) {
			throw new StoreInitializeException(e);
		} catch (SecurityException e) {
			throw new StoreInitializeException(e);
		} catch (NoSuchFieldException e) {
			throw new StoreInitializeException("Ehcache version is too low, please upgrade to version 2.5.2 or above", e);
		}
	}

}
