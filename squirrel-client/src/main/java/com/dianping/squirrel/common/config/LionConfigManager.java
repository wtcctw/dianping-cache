/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.squirrel.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.lion.EnvZooKeeperConfig;
import com.dianping.lion.client.ConfigCache;
import com.dianping.lion.client.ConfigChange;
import com.dianping.lion.client.LionException;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class LionConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerFactory.getLogger(LionConfigManager.class);

	private static String appName = null;

	private ConfigCache configCache = null;

	public LionConfigManager() {
		try {
            getConfigCache().addChange(new ConfigChange() {

                @Override
                public void onChange(String key, String value) {
                    onConfigChange(key, value);
                }
                
            });
		} catch (LionException e) {
			logger.error("", e);
		}
	}

	private ConfigCache getConfigCache() throws LionException {
		if (configCache == null) {
			synchronized (this) {
				if (configCache == null) {
					try {
						configCache = ConfigCache.getInstance();
					} catch (Exception e) {
						configCache = ConfigCache.getInstance(getConfigServerAddress());
					}
				}
			}
		}
		return configCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.pigeon.config.ConfigManager#getProperty(java.lang.String)
	 */
	@Override
	public String doGetProperty(String key) throws Exception {
		return getConfigCache().getProperty(key);
	}

	public String getConfigServerAddress() {
		return EnvZooKeeperConfig.getZKAddress();
	}

	public String doGetEnv() throws Exception {
		return EnvZooKeeperConfig.getEnv();
	}

	@Override
	public String doGetLocalProperty(String key) throws Exception {
		return null;
	}

	@Override
	public void doRegisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception {
	}
	
	@Override
	public void doUnregisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception {
	}
	
}
