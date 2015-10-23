package com.dianping.squirrel.impl;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.log.LoggerLoader;
import com.dianping.cache.config.ConfigChangeListener;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.monitor.SizeMonitor;
import com.dianping.cache.monitor.TimeMonitor;
import com.dianping.cache.status.StatusHolder;
import com.dianping.squirrel.Store;
import com.dianping.squirrel.StoreCallback;
import com.dianping.squirrel.StoreClient;
import com.dianping.squirrel.StoreKey;
import com.dianping.squirrel.exception.StoreException;

public class DefaultStoreClient implements StoreClient {

    static {
        LoggerLoader.init();
    }
    
	private static final Logger logger = LoggerFactory.getLogger(DefaultStoreClient.class);

	private static final String KEY_STORE_ENABLE = "squirrel.enable";
	private static final boolean DEFAULT_STORE_ENABLE = true;
	
	private volatile boolean enable = DEFAULT_STORE_ENABLE;
	
	private final StoreClientContainer container;

	public DefaultStoreClient() {
        this.container = new StoreClientContainer();
        this.enable = ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_STORE_ENABLE, DEFAULT_STORE_ENABLE);
        try {
            ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(KEY_STORE_ENABLE.equals(key)) {
                        enable = Boolean.parseBoolean(value);
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
        try {
            TimeMonitor.getInstance();
        } catch (Throwable t) {
            logger.warn("error while initializing time monitor", t);
        }
        try {
            SizeMonitor.getInstance();
        } catch (Throwable t) {
            logger.warn("error while initializing size monitor", t);
        }
        try {
            StatusHolder.init();
        } catch (Throwable t) {
            logger.warn("error while initializing status holder", t);
        }
	}
	
	@Override
	public <T> T get(StoreKey key) throws StoreException {
		if(enable) {
			return container.get(key);
		}
		return null;
	}

	@Override
	public boolean set(StoreKey key, Object value) throws StoreException {
		if(enable) {
			return container.set(key, value);
		}
		return false;
	}

	@Override
	public boolean add(StoreKey key, Object value) throws StoreException {
		return container.addIfAbsent(key, value);
	}

	@Override
	public boolean delete(StoreKey key) throws StoreException {
		return container.delete(key);
	}

	@Override
	public <T> Future<T> asyncGet(StoreKey key) throws StoreException {
		return container.asyncGet(key);
	}

	@Override
	public Future<Boolean> asyncSet(StoreKey key, Object value)
			throws StoreException {
		return container.asyncSet(key, value);
	}

	@Override
	public Future<Boolean> asyncAdd(StoreKey key, Object value)
			throws StoreException {
		return container.asyncAdd(key, value);
	}

	@Override
	public Future<Boolean> asyncDelete(StoreKey key) throws StoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void asyncGet(StoreKey key, StoreCallback<T> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void asyncSet(StoreKey key, Object value,
			StoreCallback<Boolean> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void asyncAdd(StoreKey key, Object value,
			StoreCallback<Boolean> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void asyncDelete(StoreKey key, StoreCallback<Boolean> callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public Store getStore(String category) {
		return null;
	}

}
