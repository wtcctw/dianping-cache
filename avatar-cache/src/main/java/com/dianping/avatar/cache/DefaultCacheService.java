/**
 * Project: avatar
 * 
 * File Created at 2010-7-14
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
package com.dianping.avatar.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.log.LoggerLoader;
import com.dianping.avatar.cache.util.CacheMonitorUtil;
import com.dianping.cache.config.ConfigChangeListener;
import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.core.CacheClient;
import com.dianping.cache.exception.CacheException;
import com.dianping.cache.monitor.SizeMonitor;
import com.dianping.cache.monitor.TimeMonitor;
import com.dianping.cache.status.StatusHolder;
import com.dianping.cache.config.ConfigManagerLoader;

/**
 * Default Cache Service Interface implement. The class should be used in
 * spring. All cache requests will be forward to {@link CacheServiceContainer}
 * 
 * @author danson.liu
 * 
 */
public class DefaultCacheService implements CacheService {

    static {
        LoggerLoader.init();
    }
    
	private static final Logger logger = LoggerFactory.getLogger(DefaultCacheService.class);

	private static final String KEY_CACHE_ENABLE = "avatar-cache.enable";
	private static final boolean DEFAULT_CACHE_ENABLE = true;
	
	private volatile boolean enable = DEFAULT_CACHE_ENABLE;
	
	/**
	 * Container
	 */
	private final CacheServiceContainer container;

	/**
	 * Default duration(3h)
	 */
	private final static int DEFAULT_EXPIRE_TIME = 3;

	public DefaultCacheService() {
        this.container = new CacheServiceContainer();
        this.enable = ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_CACHE_ENABLE, DEFAULT_CACHE_ENABLE);
        try {
            ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(KEY_CACHE_ENABLE.equals(key)) {
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
	public boolean add(String key, Object value) {
	    if(!enable) 
	        return false;
        return add(key, value, DEFAULT_EXPIRE_TIME);
	}

	@Override
	public boolean add(final String key, final Object value, final int expire) {
	    if(!enable) 
	        return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.add(key, value, expire);
				return true;
			}
		}, false, "Add item to cache with key[" + key + "] failed.");
	}

	@Override
	public boolean add(final CacheKey key, final Object value) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.asyncSet(key, value);
				return true;
			}
		}, false, "Add item to cache with cachekey[" + key + "] failed.");
	}

	@Override
	public boolean add(final Object entity) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.add(entity);
				return true;
			}
		}, false, "Add entity[" + entity + "] to cache failed.");
	}

	public <T> boolean mAdd(final List<T> entities) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.mAdd(entities);
				return true;
			}
		}, false, "MAdd entities to cache failed.");
	}

	@Override
	public <T> boolean mAdd(final CacheKey cacheKey, final List<T> entities) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.mAdd(cacheKey, entities);
				return true;
			}
		}, false, "MAdd entities with cachekey[" + cacheKey + "] failed.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final String key) {
	    if(!enable) 
            return null;
		return (T) executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.get(key);
			}
		}, null, "Get item from cache with key[" + key + "] failed.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final CacheKey key) {
	    if(!enable) 
            return null;
		return (T) executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.get(key);
			}
		}, null, "Get item from cache with cachekey[" + key + "] failed.");
	}

	public <T> T get(final CacheKey key, final String finalKey) {
	    if(!enable) 
            return null;
		return (T) executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.get(key, finalKey);
			}
		}, null, "Get item from cache with cachekey[" + key + "] failed.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Class<?> cz, final List<?> params) {
	    if(!enable) 
            return null;
		return (T) executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.get(cz, params);
			}
		}, null, "Get entity[type=" + cz.getSimpleName() + "] with index params[" + params + "] failed.");
	}

	@Override
	public <T> List<T> mGet(final Class<?> cz, final List<?> params) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(cz, params);
			}
		}, null, "MGet entities[type=" + cz.getSimpleName() + "] failed.");
	}

	@Override
	public <T> List<T> mGet(final EntityKey... keys) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(keys);
			}
		}, null, "MGet entities by entitykeys failed.");
	}

	@Override
	public <T> List<T> mGet(final List<CacheKey> keys) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(keys);
			}
		}, null, "MGet entities by cachekeys failed.");
	}

	@Override
	public <T> List<T> mGet(final List<CacheKey> keys, final boolean returnNullIfAnyKeyMissed) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(keys, returnNullIfAnyKeyMissed);
			}
		}, null, "MGet entities by cachekeys failed.");
	}

	@Override
	public <T> Map<String, T> mGet(final Set<String> keys) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(keys);
			}
		}, null, "MGet items with keys[" + keys + "] failed.");
	}

	@Override
	public <T> List<T> mGet(final CacheKey cacheKey) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGet(cacheKey);
			}
		}, null, "MGet items with cachekey[" + cacheKey + "] failed.");
	}

	@Override
	public boolean remove(final CacheKey cacheKey) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.asyncDelete(cacheKey);
				return true;
			}
		}, false, "Remove item from cache with cachekey[" + cacheKey + "] failed.");
	}

	@Override
	public boolean remove(final String cacheType, final String key) {
	    if(!enable) 
            return false;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				container.asyncDelete(cacheType, key);
				return true;
			}
		}, false, "Remove item from cache with key[" + key + "] failed.");
	}

	@Override
	public String getFinalKey(CacheKey key) {
		return container.getFinalKey(key);
	}

	@SuppressWarnings("unchecked")
	private <T> T executeWithNoError(Command command, T returnValueIfError, String errorMsg) {
		try {
			return (T) command.execute();
		} catch (Throwable throwable) {
			CacheMonitorUtil.logCacheError(errorMsg, throwable);
			return returnValueIfError;
		}
	}

	@Override
	public <T> Map<CacheKey, T> mGetWithNonExists(final List<CacheKey> keys) {
	    if(!enable) 
            return null;
		return executeWithNoError(new Command() {
			@Override
			public Object execute() throws Exception {
				return container.mGetWithNonExists(keys);
			}
		}, new HashMap<CacheKey, T>(), "MGet entities by cachekeys failed.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.avatar.cache.CacheService#getWithTimeoutAware(com.dianping
	 * .avatar.cache.CacheKey)
	 */
	@Override
	public <T> T getOrTimeout(CacheKey key) throws TimeoutException {
	    if(!enable) 
            return null;
		try {
			return (T) container.get(key);
		} catch (TimeoutException e) {
			throw e;
		} catch (Throwable e) {
			CacheMonitorUtil.logCacheError("Get item from cache with cachekey[" + key + "] failed.", e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.dianping.avatar.cache.CacheService#mGetOrTimeout(com.dianping.avatar
	 * .cache.CacheKey)
	 */
	@Override
	public <T> Map<CacheKey, T> mGetOrTimeout(List<CacheKey> keys) throws TimeoutException {
	    if(!enable) 
            return null;
		try {
			return container.mGetWithTimeoutAware(keys);
		} catch (TimeoutException e) {
			throw e;
		} catch (Throwable e) {
			CacheMonitorUtil.logCacheError("Get item from cache with cachekey[" + keys + "] failed.", e);
			return null;
		}
	}

	@Override
	public long increment(final CacheKey key, final int amount) throws CacheException, TimeoutException {
	    if(!enable) 
            throw new CacheException("avatar cache is disabled");
		return container.increment(key, amount);
	}

	@Override
	public long decrement(final CacheKey key, final int amount) throws CacheException, TimeoutException {
	    if(!enable) 
            throw new CacheException("avatar cache is disabled");;
		return container.decrement(key, amount);
	}

	@Override
	public long increment(final CacheKey key, final int amount, long defaultValue) throws CacheException,
			TimeoutException {
	    if(!enable) 
	        throw new CacheException("avatar cache is disabled");
		return container.increment(key, amount, defaultValue);
	}

	@Override
	public long decrement(final CacheKey key, final int amount, long defaultValue) throws CacheException,
			TimeoutException {
	    if(!enable) 
	        throw new CacheException("avatar cache is disabled");
		return container.decrement(key, amount, defaultValue);
	}

	@Override
	public boolean addIfAbsent(final CacheKey key, final Object value) throws CacheException, TimeoutException {
	    if(!enable) 
            return false;
		return container.addIfAbsent(key, value);
	}

	@Override
	public boolean addIfAbsent(final CacheKey key, final Object value, final long timeout) throws CacheException,
			TimeoutException {
	    if(!enable) 
            return false;
		return container.addIfAbsent(key, value, timeout);
	}

	@Override
	public void addIfAbsentWithNoReply(final CacheKey key, final Object value) throws CacheException {
	    if(!enable) 
            return;
		container.asyncAddIfAbsent(key, value);
	}

	@Override
	public <T> CASValue<T> gets(CacheKey key) throws CacheException, TimeoutException {
	    if(!enable) 
            return null;
		return container.gets(key);
	}

	@Override
	public CASResponse cas(CacheKey key, long casId, Object value) throws CacheException, TimeoutException {
	    if(!enable) 
            return null;
		return container.cas(key, casId, value);
	}

	@Override
	public boolean set(CacheKey key, Object value) throws CacheException, TimeoutException {
	    if(!enable) 
            return false;
		return container.set(key, value);
	}

	@Override
	public boolean set(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException {
	    if(!enable) 
            return false;
		return container.set(key, value, timeout);
	}

	@Override
	public Future<Boolean> asyncSet(final CacheKey key, final Object value) throws CacheException {
	    if(!enable) 
            return new NullFuture<Boolean>();
		return container.asyncSet(key, value);
	}

	@Override
	public <T> void asyncGet(final CacheKey key, final CacheCallback<T> callback) {
	    if(!enable) {
	        if(callback != null) {
	            callback.onSuccess(null);
	        }
            return;
	    }
		container.asyncGet(key, callback);
	}

	@Override
	public <T> void asyncBatchGet(final List<CacheKey> keys, final CacheCallback<Map<CacheKey, T>> callback) {
	    if(!enable) {
	        if(callback != null) {
                callback.onSuccess(null);
            }
            return;
	    }
		container.asyncBatchGet(keys, callback);
	}
	
	@Override
	public <T> void asyncBatchSet(final List<CacheKey> keys, final List<T> values, final CacheCallback<Boolean> callback) {
	    if(!enable) {
            if(callback != null) {
                callback.onSuccess(null);
            }
            return;
        }
	    container.asyncBatchSet(keys, values, callback);
	}
	
	@Override
    public <T> boolean batchSet(final List<CacheKey> keys, final List<T> values) throws TimeoutException, CacheException {
	    if(!enable) {
            return false;
        }
        return container.batchSet(keys, values);
    }


	@Override
	public void asyncSet(final CacheKey key, final Object value, final CacheCallback<Boolean> callback) {
	    if(!enable) {
            if(callback != null) {
                callback.onSuccess(null);
            }
            return;
        }
		container.asyncSet(key, value, callback);
	}

	@Override
	public Future<Boolean> asyncAddIfAbsent(final CacheKey key, final Object value) throws CacheException {
	    if(!enable) {
            return new NullFuture<Boolean>();
        }
		return container.asyncAddIfAbsent(key, value);
	}

	@Override
	public void asyncAddIfAbsent(final CacheKey key, final Object value, final CacheCallback<Boolean> callback) {
	    if(!enable) {
            if(callback != null) {
                callback.onSuccess(null);
            }
            return;
        }
		container.asyncAddIfAbsent(key, value, callback);
	}

	@Override
	public <T> Future<T> asyncGet(CacheKey key) throws CacheException {
	    if(!enable) {
            return new NullFuture<T>();
        }
		return container.asyncGet(key);
	}

	@Override
	public boolean delete(CacheKey key) throws CacheException, TimeoutException {
	    if(!enable) {
            return false;
        }
		return container.delete(key);
	}

	@Override
	public Future<Boolean> asyncDelete(CacheKey key) throws CacheException {
	    if(!enable) {
            return new NullFuture<Boolean>();
        }
		return container.asyncDelete(key);
	}

	@Override
	public CacheClient getCacheClient(CacheKey key) {
		return container.getCacheClient(key);
	}

	@Override
	public CacheClient getCacheClient(String cacheType) {
		return container.getCacheClient(cacheType);
	}

	/**
	 * Generic Command Interface
	 * @author danson.liu
	 *
	 */
	public interface Command {

	    Object execute() throws Exception;
	    
	}
	
	public class NullFuture<T> implements Future<T> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
	    
	}

}