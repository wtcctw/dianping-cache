/**
 * Project: cache-core
 * 
 * File Created at 2010-9-1 $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with dianping.com.
 */
package com.dianping.squirrel.client.impl.ehcache;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServer;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.management.ManagementService;

import com.dianping.squirrel.client.core.CASResponse;
import com.dianping.squirrel.client.core.CASValue;
import com.dianping.squirrel.client.core.CacheCallback;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreFuture;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.google.common.eventbus.EventBus;

/**
 * EhcacheClientImpl 4 avatar local cache!
 * 
 * @author pengshan.zhang
 * @author jinhua.liang
 * 
 */
public class EhcacheClientImpl implements CacheClient, Lifecycle {

	public static final EventBus eventBus = new EventBus();

	/**
	 * Template cache name
	 */
	private static final String TEMPLATE_CACHE_NAME = "templateCache";

	private static final int hotkeyLockTime = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.ehcache.hotkey.locktime", 30);

	/**
	 * Ehcache CacheManager instance
	 */
	private static CacheManager manager;

	private BlockingCache defaultBlockingCache;

	public static void publishCacheManager() {
		eventBus.post(new EhcacheEvent(manager));
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#add()
	 */
	@Override
	public boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		if (!findCache(category).isKeyInCache(key)) {
			findCache(category).put(
					new Element(key, value, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(expiration)));
			return true;
		}
		return false;
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#clear()
	 */
	@Override
	public void clear() {
		defaultBlockingCache.removeAll();
		for (String cacheName : manager.getCacheNames()) {
			manager.getCache(cacheName).removeAll();
		}
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#decrement(java.lang.String, int)
	 */
	@Override
	public long decrement(String key, int amount, String category) {
		Element element = findCache(category).get(key);
		if (element != null) {
			Object value = element.getObjectValue();
			if (value instanceof Long) {
				findCache(category).remove(key);
				long newValue = (Long) value - amount;
				findCache(category).put(
						new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
								.getTimeToLive())));
				return newValue;
			}
		}
		return -1;
	}

	public <T> T get(String key, String category, boolean timeoutAware) {
		Element element = findCache(category).get(key);
		return (T) (element == null ? null : element.getObjectValue());
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#increment(java.lang.String, int)
	 */
	@Override
	public long increment(String key, int amount, String category) {
		Element element = findCache(category).get(key);
		if (element != null) {
			Object value = element.getObjectValue();
			if (value instanceof Long) {
				findCache(category).remove(key);
				long newValue = (Long) value + amount;
				findCache(category).put(
						new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
								.getTimeToLive())));
				return newValue;
			}
		}
		return -1;
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#remove(java.lang.String)
	 */
	@Override
	public boolean delete(String key, boolean isHot, String category) throws StoreException, TimeoutException {
		return findCache(category).remove(key);
	}

	@Override
	public boolean delete(String key, boolean isHot, String category, long timeout) throws StoreException,
			TimeoutException {
		return findCache(category).remove(key);
	}

	public Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws StoreException {
		boolean result = findCache(category).remove(key);
		StoreFuture<Boolean> future = new StoreFuture<Boolean>(key);
		future.onSuccess(result);
		return future;
	}

	/**
	 * @see com.dianping.squirrel.client.core.CacheClient#replace(java.lang.String,
	 *      java.lang.Object, int)
	 */
	@Override
	public void replace(String key, Object value, int expiration, boolean isHot, String category) {
		if (findCache(category).isKeyInCache(key)) {
			findCache(category).put(
					new Element(key, value, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(expiration)));
		}
	}

	/**
	 * @see com.dianping.squirrel.client.core.Lifecycle#stop()
	 */
	@Override
	public void stop() {
		manager.shutdown();
	}

	/**
	 * @see com.dianping.squirrel.client.core.Lifecycle#start()
	 */
	@Override
	public void start() {
		Ehcache cache = manager.getCache(TEMPLATE_CACHE_NAME);
		defaultBlockingCache = new LooseBlockingCache(cache);
		manager.replaceCacheWithDecoratedCache(cache, defaultBlockingCache);
	}

	/**
	 * @see com.dianping.squirrel.client.core.InitialConfiguration#initialize(com.dianping.squirrel.client.core.StoreClientConfig)
	 */
	@Override
	public void initialize(StoreClientConfig config) {
		if (config instanceof EhcacheConfiguration) {
			manager = ((EhcacheConfiguration) config).buildEhcacheManager();
		}
		if (manager == null) {
			manager = CacheManager.create();
		}
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		ManagementService.registerMBeans(manager, server, true, true, true, true);
	}

	@Override
	public boolean isDistributed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cache.core.CacheClient#get(java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, Class dataType, boolean isHot, String category, boolean timeoutAware) {
		T result = (T) get(key, category, timeoutAware);

		if (isHot) {
			if (result == null) {
				Element element = findCache(category).putIfAbsent(
						new Element(key + "_lock", true, Boolean.FALSE, Integer.valueOf(0), Integer
								.valueOf(getHotkeyLockTime())));
				boolean locked = (element == null);
				// try {
				// lock.lock();
				// if (findCache(category).get(key + "_lock") == null) {
				// findCache(category).put(
				// new Element(key + "_lock", true, Boolean.FALSE,
				// Integer.valueOf(0), Integer
				// .valueOf(getHotkeyLockTime())));
				// locked = true;
				// }
				// } finally {
				// lock.unlock();
				// }

				if (locked) {
					return null;
				} else {
					// 批量清理时，因为version升级了，所以bak数据要考虑从上一个版本中查找
					result = (T) get(key + "_bak", category, timeoutAware);
					if (result == null) {
						String lastVersionKey = genLastVersionCacheKey(key);
						if (!key.equals(lastVersionKey)) {
							result = (T) get(lastVersionKey + "_bak", category, timeoutAware);
						}
					}
					return result;
				}
			}
		}

		return result;
	}

	private String genLastVersionCacheKey(String currentVersionCacheKey) {
		if (currentVersionCacheKey == null) {
			return currentVersionCacheKey;
		}

		int versionSplitPos = currentVersionCacheKey.lastIndexOf("_");
		if (versionSplitPos < 0) {
			return currentVersionCacheKey;
		}

		String versionStr = currentVersionCacheKey.substring(versionSplitPos + 1);
		if (!isNumeric(versionStr)) {
			return currentVersionCacheKey;
		}

		Integer currentVersion = Integer.valueOf(versionStr);
		if (currentVersion > 0) {
			return currentVersionCacheKey.substring(0, versionSplitPos + 1) + (currentVersion - 1);
		} else {
			return currentVersionCacheKey;
		}

	}

	private boolean isNumeric(String src) {
		if (src == null || src.length() == 0) {
			return false;
		}

		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) < '0' || src.charAt(i) > '9') {
				return false;
			}
		}

		return true;
	}

	private int getHotkeyLockTime() {
		return hotkeyLockTime;
	}

	private Ehcache findCache(String category) {
		if (category == null) {
			return defaultBlockingCache;
		}
		Ehcache cache = manager.getCache(category);
		if (cache == null) {
			return defaultBlockingCache;
		}
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cache.core.CacheClient#get(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public <T> T get(String key, Class dataType, String category) {
		return (T) get(key, category, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cache.core.CacheClient#getBulk(java.util.Collection,
	 * java.util.Map, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Map<String, T> getBulk(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			boolean timeoutAware) {
		Map<String, T> map = new HashMap<String, T>();
		for (String key : keys) {
			Element element = findCache(categories == null ? null : categories.get(key)).get(key);
			map.put(key, (element == null ? null : (T) element.getObjectValue()));
		}
		return map;
	}

	@Override
	public Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		boolean result = false;
		if (!findCache(category).isKeyInCache(key)) {
			findCache(category).put(
					new Element(key, value, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(expiration)));
			result = true;
		}
		StoreFuture<Boolean> future = new StoreFuture<Boolean>(key);
		future.onSuccess(result);
		return future;
	}

	@Override
	public boolean add(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return add(key, value, expiration, -1, isHot, category);
	}

	@Override
	public long increment(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		Element element = findCache(category).get(key);
		if (element != null) {
			Object value = element.getObjectValue();
			if (value instanceof Long) {
				findCache(category).remove(key);
				long newValue = (Long) value + amount;
				findCache(category).put(
						new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
								.getTimeToLive())));
				return newValue;
			} else {
				return -1;
			}
		} else {
			long newValue = def;
			findCache(category).put(
					new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
							.getTimeToLive())));
			return newValue;
		}
	}

	@Override
	public long decrement(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		Element element = findCache(category).get(key);
		if (element != null) {
			Object value = element.getObjectValue();
			if (value instanceof Long) {
				findCache(category).remove(key);
				long newValue = (Long) value - amount;
				findCache(category).put(
						new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
								.getTimeToLive())));
				return newValue;
			} else {
				return -1;
			}
		} else {
			long newValue = def;
			findCache(category).put(
					new Element(key, newValue, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(element
							.getTimeToLive())));
			return def;
		}
	}

	@Override
	public <T> CASValue<T> gets(String key, String category) throws StoreException, TimeoutException {
		throw new StoreException("operation not supported");
	}

	@Override
	public CASResponse cas(String key, long casId, Object value, String category) throws StoreException,
			TimeoutException {
		throw new StoreException("operation not supported");
	}

	@Override
	public boolean set(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return this.set(key, value, expiration, -1l, isHot, category);
	}

	@Override
	public boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		this.asyncSet(key, value, expiration, isHot, category);
		return true;
	}

	@Override
	public Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		findCache(category)
				.put(new Element(key, value, Boolean.FALSE, Integer.valueOf(0), Integer.valueOf(expiration)));
		if (isHot) {
			findCache(category).put(
					new Element(key + "_bak", value, Boolean.TRUE, Integer.valueOf(0), Integer.valueOf(0)));
			String lastVersionKey = genLastVersionCacheKey(key);
			// 当版本升级后，要清理上一个版本的hotkey数据
			if (!key.equals(lastVersionKey)) {
				findCache(category).remove(lastVersionKey + "_bak");
			}
		}
		StoreFuture<Boolean> future = new StoreFuture<Boolean>(key);
		future.onSuccess(true);
		return future;
	}

	@Override
	public void asyncSet(String key, Object value, int expiration, boolean isHot, String category,
			final CacheCallback<Boolean> callback) {
		try {
			asyncSet(key, value, expiration, isHot, category);
			if (callback != null) {
				callback.onSuccess(true);
			}
		} catch (Throwable e) {
			if (callback != null) {
				callback.onFailure("", e);
			}
		}
	}

	@Override
	public void asyncAdd(String key, Object value, int expiration, boolean isHot, String category,
			CacheCallback<Boolean> callback) {
		try {
			boolean result = add(key, value, expiration, isHot, category);
			if (callback != null) {
				callback.onSuccess(result);
			}
		} catch (Throwable e) {
			if (callback != null) {
				callback.onFailure("", e);
			}
		}
	}

	@Override
	public <T> Future<T> asyncGet(String key, Class dataType, boolean isHot, String category) throws StoreException {
		T result = (T) get(key, category, false);
		StoreFuture<T> future = new StoreFuture<T>(key);
		future.onSuccess(result);
		return future;
	}

	@Override
	public <T> void asyncGet(String key, Class dataType, boolean isHot, String category, CacheCallback<T> callback) {
		try {
			T result = (T) get(key, category, false);
			if (callback != null) {
				callback.onSuccess(result);
			}
		} catch (Throwable e) {
			if (callback != null) {
				callback.onFailure("", e);
			}
		}
	}

	@Override
	public <T> void asyncBatchGet(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			CacheCallback<Map<String, T>> callback) {
		try {
			Map<String, T> results = getBulk(keys, dataType, isHot, categories, false);
			if (callback != null) {
				callback.onSuccess(results);
			}
		} catch (Throwable e) {
			if (callback != null) {
				callback.onFailure("", e);
			}
		}
	}

    @Override
    public <T> void asyncBatchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category,
            CacheCallback<Boolean> callback) {
        try {
            for(int i=0; i<keys.size(); i++) {
                String key = keys.get(i);
                T value = values.get(i);
                set(key, value, expiration, isHot, category);
            }
        } catch (Exception e) {
            if(callback != null) {
                callback.onFailure("ehcache async batch set failed", e);
            }
        }
        if(callback != null) {
            callback.onSuccess(true);
        }
    }
    
    @Override
    public <T> boolean batchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category)
    throws TimeoutException, StoreException {
        for(int i=0; i<keys.size(); i++) {
            String key = keys.get(i);
            T value = values.get(i);
            set(key, value, expiration, isHot, category);
        }
        return true;
    }
    
}