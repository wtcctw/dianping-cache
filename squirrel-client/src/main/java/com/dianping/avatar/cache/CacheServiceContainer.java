/**
 * Project: avatar
 * 
 * File Created at 2010-7-15 $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Dianping.com.
 */
package com.dianping.avatar.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.avatar.cache.CacheService.EntityKey;
import com.dianping.cache.exception.CacheException;
import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.remote.cache.CacheManageWebService;
import com.dianping.remote.cache.dto.CacheClearDTO;
import com.dianping.squirrel.client.annotation.Cache;
import com.dianping.squirrel.client.config.CacheClientFactory;
import com.dianping.squirrel.client.config.CacheItemConfigManager;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.RemoteCacheClientFactory;
import com.dianping.squirrel.client.config.RemoteCacheItemConfigManager;
import com.dianping.squirrel.client.core.CASResponse;
import com.dianping.squirrel.client.core.CASValue;
import com.dianping.squirrel.client.core.CacheCallback;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.monitor.HitRateMonitor;
import com.dianping.squirrel.client.monitor.KeyCountMonitor;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.client.monitor.TimeMonitor;
import com.dianping.squirrel.client.util.CacheAnnotationUtils;
import com.dianping.squirrel.client.util.CacheMonitorUtil;
import com.dianping.squirrel.client.util.CacheTracker;
import com.dianping.squirrel.client.util.DefaultCacheTracker;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.site.helper.Stringizers;

/**
 * Cache service container
 * 
 * @author danson.liu
 * @author guoqing.chen
 */
public class CacheServiceContainer {

	/**
	 * Default cache
	 */
	private String defaultCacheType = CacheKeyType.DEFAULT_STORE_TYPE;

	/**
	 * Default cache category
	 */
	private String defaultCategory = "Default";

	/**
	 * Cache client factory
	 */
	private CacheClientFactory cacheClientFactory;

	private CacheManageWebService cacheManageWebService;

	/**
	 * retrieve cache item's configuration
	 */
	private CacheItemConfigManager configManager;

	private CacheTracker cacheTracker = new DefaultCacheTracker();

	private static final boolean enableTracker = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"avatar-cache.tracker.enable", true);

	private static final boolean enableLogWeb = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"avatar-cache.log.web.enable", false);

	public CacheServiceContainer() {
        configManager = RemoteCacheItemConfigManager.getInstance();

        cacheClientFactory = RemoteCacheClientFactory.getInstance();

        InvokerConfig<CacheManageWebService> config = new InvokerConfig<CacheManageWebService>(
                "http://service.dianping.com/cacheService/cacheManageService_1.0.0", CacheManageWebService.class);
        config.setCallType("oneway");
        config.setTimeout(10000);
        cacheManageWebService = ServiceFactory.getService(config);
	}

	public void add(String key, Object value, int expire) throws Exception {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		Transaction t = Cat.getProducer().newTransaction("Cache." + defaultCacheType, defaultCategory + ":asyncSet");
		t.setStatus(Message.SUCCESS);
		String statusKey = "asyncSet";
		StatusHolder.flowIn(defaultCacheType, defaultCategory, statusKey);
		try {
			CacheClient cacheClient = getCacheClient(defaultCacheType);
			cacheClient.asyncSet(key, value, expire * 60 * 60, false, null);
			t.addData("finalKey", key);
		} catch (Exception e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} finally {
			t.complete();
			StatusHolder.flowOut(defaultCacheType, defaultCategory, statusKey);
		}
	}

	public Future<Boolean> asyncSet(final CacheKey key, final Object value) throws CacheException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncSet");
		t.setStatus(Message.SUCCESS);
		String statusKey = "asyncSet";
		StatusHolder.flowIn(cacheType, category, statusKey);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.asyncSet(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
					cacheKeyType.getCategory());
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public void asyncSet(final CacheKey key, final Object value, final CacheCallback<Boolean> callback) {
		if (key == null || value == null || callback == null) {
			throw new IllegalArgumentException("invalid parameters");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncSet");
		t.setStatus(Message.SUCCESS);
		String statusKey = "asyncSet";
		StatusHolder.flowIn(cacheType, category, statusKey);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			cacheClient.asyncSet(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
					cacheKeyType.getCategory(), callback);
			t.addData("finalKey", finalKey);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public <T> void add(T entity) throws Exception {
		asyncSet(getCacheKey(entity), entity);
	}

	public boolean addIfAbsent(CacheKey key, Object value) throws CacheException, TimeoutException {
		return addIfAbsent(key, value, -1);
	}

	public boolean addIfAbsent(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":add");
		t.setStatus(Message.SUCCESS);
		String statusKey = "add";
		StatusHolder.flowIn(cacheType, category, statusKey);
		long begin = System.nanoTime();
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			boolean ret = false;
			if (timeout <= 0) {
				ret = cacheClient.add(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
						cacheKeyType.getCategory());
			} else {
				ret = cacheClient.add(finalKey, value, cacheKeyType.getDurationSeconds(), timeout,
						cacheKeyType.isHot(), cacheKeyType.getCategory());
			}
			TimeMonitor.getInstance().logTime(cacheType, category, "add", System.nanoTime() - begin);
			return ret;
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			TimeMonitor.getInstance().logTime(cacheType, category, "add", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public Future<Boolean> asyncAddIfAbsent(final CacheKey key, final Object value) throws CacheException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncAdd");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.asyncAdd(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
					cacheKeyType.getCategory());
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public void asyncAddIfAbsent(final CacheKey key, final Object value, final CacheCallback<Boolean> callback) {
		if (key == null || value == null || callback == null) {
			throw new IllegalArgumentException("invalid parameters");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncAdd");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			cacheClient.asyncAdd(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
					cacheKeyType.getCategory(), callback);
		} finally {
			t.complete();
		}
	}

	public <T> void mAdd(List<T> entities) throws Exception {
		if (entities == null) {
			throw new IllegalArgumentException("entities is null.");
		}

		for (T entity : entities) {
			add(entity);
		}
	}

	public <T> void mAdd(CacheKey cacheKey, List<T> objects) throws Exception {
		if (objects == null) {
			throw new IllegalArgumentException("objs is null.");
		}

		final List<CacheKey> keys = new ArrayList<CacheKey>();

		for (T object : objects) {
			keys.add(getCacheKey(object));
			add(object);
		}

		asyncSet(cacheKey, keys);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) throws Exception {
		if (key == null) {
			throw new IllegalArgumentException("Key is null.");
		}
		Transaction t = Cat.getProducer().newTransaction("Cache." + defaultCacheType, defaultCategory + ":get");
		t.setStatus(Message.SUCCESS);
		String statusKey = "get";
		StatusHolder.flowIn(defaultCacheType, defaultCategory, statusKey);
		try {
			CacheClient cacheClient = getCacheClient(defaultCacheType);
			long begin = System.nanoTime();
			Object cachedItem = cacheClient.get(key, Object.class, null);
			long end = System.nanoTime();
			if (cachedItem != null) {
				if (enableTracker) {
					try {
						cacheTracker.addGetInfo(key + "[" + defaultCacheType + "]", end - begin);
					} catch (Throwable e) {
						CacheMonitorUtil.logCacheError("error with cache tracker", e);
					}
				}
			} else {
				Cat.getProducer().logEvent("Cache." + defaultCacheType, defaultCategory + ":missed", Message.SUCCESS,
						"");
			}
			t.addData("finalKey", key);
			TimeMonitor.getInstance().logTime(defaultCacheType, defaultCategory, "get", end - begin);
			return (T) cachedItem;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new CacheException(e.getMessage(), e);
			}
		} finally {
			t.complete();
			StatusHolder.flowOut(defaultCacheType, defaultCategory, statusKey);
		}
	}

	boolean needLog(String cacheType) {
		if (!enableLogWeb && "web".equalsIgnoreCase(cacheType)) {
			return false;
		}
		return true;
	}

	public <T> T get(CacheKey key) throws Exception {
		if (key == null) {
			throw new IllegalArgumentException("CacheKey is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();
		String finalKey = cacheKeyType.getKey(key.getParams());

		return get(cacheType, category, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), finalKey);
	}

	public <T> T get(CacheKey key, String finalKey) throws Exception {
		if (key == null) {
			throw new IllegalArgumentException("CacheKey is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		return get(cacheType, category, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), finalKey);
	}

	@SuppressWarnings("unchecked")
	private <T> T get(String cacheType, String category, Class dataType, boolean isHot, String finalKey) throws Exception {
		if (finalKey == null) {
			throw new IllegalArgumentException("CacheKey is null.");
		}
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":get");
			t.setStatus(Message.SUCCESS);
		}
		String statusKey = "get";
		StatusHolder.flowIn(cacheType, category, statusKey);
		long begin = System.nanoTime();
		try {
			CacheClient cacheClient = getCacheClient(cacheType);
			Object cachedItem = cacheClient.get(finalKey, dataType, isHot, category, true);
			long end = System.nanoTime();
			if (cachedItem != null) {
				if (enableTracker) {
					try {
						cacheTracker.addGetInfo(finalKey + "[" + cacheType + "]", end - begin);
					} catch (Throwable e) {
						CacheMonitorUtil.logCacheError("error with cache tracker", e);
					}
				}
			} else {
				Cat.getProducer().logEvent("Cache." + cacheType, category + ":missed", Message.SUCCESS, "");
			}
			if (t != null) {
				t.addData("finalKey", finalKey);
			}
			TimeMonitor.getInstance().logTime(cacheType, category, "get", end - begin);
			return (T) cachedItem;
		} catch (TimeoutException e) {
			Cat.getProducer().logEvent("Cache." + cacheType, category + ":timeout", Message.SUCCESS, "");
			TimeMonitor.getInstance().logTime(cacheType, category, "get", System.nanoTime() - begin, "timeout");
			if (t != null) {
				t.addData("finalKey", finalKey);
				t.setStatus(e);
			}
			throw e;
		} catch (Throwable e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logError(e);
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new CacheException(e.getMessage(), e);
			}
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public <T> Future<T> asyncGet(CacheKey key) throws CacheException {
		if (key == null) {
			throw new IllegalArgumentException("CacheKey is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();
		String finalKey = cacheKeyType.getKey(key.getParams());
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncGet");
			t.setStatus(Message.SUCCESS);
		}
		String statusKey = "asyncGet";
		StatusHolder.flowIn(cacheType, category, statusKey);
		try {
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			Future<T> future = cacheClient.asyncGet(finalKey, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), cacheKeyType.getCategory());
			if (t != null) {
				t.addData("finalKey", finalKey);
			}
			return future;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public <T> void asyncGet(CacheKey key, final CacheCallback<T> callback) {
		if (key == null) {
			throw new IllegalArgumentException("CacheKey is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();
		String finalKey = cacheKeyType.getKey(key.getParams());
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":asyncGet");
			t.setStatus(Message.SUCCESS);
		}
		String statusKey = "asyncGet";
		StatusHolder.flowIn(cacheType, category, statusKey);
		try {
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			cacheClient.asyncGet(finalKey, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), cacheKeyType.getCategory(), callback);
			if (t != null) {
				t.addData("finalKey", finalKey);
			}
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public <T> void asyncBatchGet(final List<CacheKey> keys, final CacheCallback<Map<CacheKey, T>> callback) {
		if (keys == null) {
			throw new IllegalArgumentException("Parameter keys is null.");
		}
		if (keys.isEmpty()) {
			return;
		}
		final List<String> finalKeys = new ArrayList<String>();
		final Map<String, String> categories = new HashMap<String, String>();
		final Map<String, CacheKey> finalKeyCacheKeyMapping = new HashMap<String, CacheKey>();
		final CacheKeyType cacheKeyType = getCacheKeyMetadata(keys.get(0));
		final String cacheType = cacheKeyType.getCacheType();
		final String category = keys.get(0).getCategory();
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":mGet");
			t.setStatus(Message.SUCCESS);
		}
		String statusKey = "mget";
		StatusHolder.flowIn(cacheType, category, statusKey);
		try {
			for (CacheKey key : keys) {
				String finalKey = cacheKeyType.getKey(key.getParams());
				finalKeys.add(finalKey);
				categories.put(finalKey, cacheKeyType.getCategory());
				finalKeyCacheKeyMapping.put(finalKey, key);
			}

			KeyCountMonitor.getInstance().logKeyCount(cacheType, category, "mget", keys.size());

			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());

			CacheCallback<Map<String, T>> innerCallback = new CacheCallback<Map<String, T>>() {

				@Override
				public void onSuccess(Map<String, T> result) {
					Map<String, T> cachedDataMap = result;
					int hits = cachedDataMap == null ? 0 : cachedDataMap.size();
					HitRateMonitor.getInstance().logHitRate(cacheType, category, "mget",
							(int) (100 * ((float) (hits) / finalKeyCacheKeyMapping.size())), hits);

					Map<CacheKey, T> res = new HashMap<CacheKey, T>();
					if (cachedDataMap == null) {
						cachedDataMap = new HashMap<String, T>();
					}
					for (String finalKey : finalKeys) {
						res.put(finalKeyCacheKeyMapping.get(finalKey), cachedDataMap.get(finalKey));
					}

					callback.onSuccess(res);
				}

				@Override
				public void onFailure(String msg, Throwable e) {
					callback.onFailure(msg, e);
				}

			};
			cacheClient.asyncBatchGet(finalKeys, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), categories, innerCallback);
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public <T> void asyncBatchSet(final List<CacheKey> keys, final List<T> values, final CacheCallback<Boolean> callback) {
	    if(keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("key list is empty");
        }
        if(keys.size() != values.size()) {
            throw new IllegalArgumentException("key size does not match value size");
        }
        
        final List<String> finalKeys = new ArrayList<String>();
        final CacheKeyType cacheKeyType = getCacheKeyMetadata(keys.get(0));
        final String cacheType = cacheKeyType.getCacheType();
        final String category = keys.get(0).getCategory();
        Transaction t = null;
        if (needLog(cacheType)) {
            t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":mSet");
            t.setStatus(Message.SUCCESS);
        }
        String statusKey = "mset";
        StatusHolder.flowIn(cacheType, category, statusKey);
        try {
            for (CacheKey key : keys) {
                String finalKey = cacheKeyType.getKey(key.getParams());
                finalKeys.add(finalKey);
            }

            KeyCountMonitor.getInstance().logKeyCount(cacheType, category, "mset", keys.size());

            CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());

            CacheCallback<Boolean> innerCallback = new CacheCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean success) {
                    if(callback != null)
                        callback.onSuccess(true);
                }

                @Override
                public void onFailure(String msg, Throwable e) {
                    if(callback != null) {
                        callback.onFailure(msg, e);
                    }
                }

            };
            cacheClient.asyncBatchSet(finalKeys, values, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(), category, innerCallback);
        } finally {
            if (t != null) {
                t.complete();
            }
            StatusHolder.flowOut(cacheType, category, statusKey);
        }
    }
	
	public <T> boolean batchSet(final List<CacheKey> keys, final List<T> values) throws TimeoutException, CacheException {
        if(keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("key list is empty");
        }
	    if(keys.size() != values.size()) {
	        throw new IllegalArgumentException("key size does not match value size");
	    }
        
        final List<String> finalKeys = new ArrayList<String>();
        final CacheKeyType cacheKeyType = getCacheKeyMetadata(keys.get(0));
        final String cacheType = cacheKeyType.getCacheType();
        final String category = keys.get(0).getCategory();
        Transaction t = null;
        if (needLog(cacheType)) {
            t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":mSet");
            t.setStatus(Message.SUCCESS);
        }
        String statusKey = "mset";
        StatusHolder.flowIn(cacheType, category, statusKey);
        try {
            for (CacheKey key : keys) {
                String finalKey = cacheKeyType.getKey(key.getParams());
                finalKeys.add(finalKey);
            }

            KeyCountMonitor.getInstance().logKeyCount(cacheType, category, "mset", keys.size());

            CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());

            return cacheClient.batchSet(finalKeys, values, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(), category);
        } finally {
            if (t != null) {
                t.complete();
            }
            StatusHolder.flowOut(cacheType, category, statusKey);
        }
    }
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<?> cz, List<?> params) throws Exception {
		String category = CacheAnnotationUtils.getCacheCategory(cz);
		CacheKey cacheKey = new CacheKey(category, params.toArray(new Object[params.size()]));

		return (T) get(cacheKey);
	}

	public <T> List<T> mGet(Class<?> cz, List<?> params) throws Exception {
		if (params == null || params.isEmpty()) {
			throw new IllegalArgumentException("params must not be null or empty.");
		}
		String category = CacheAnnotationUtils.getCacheCategory(cz);
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		for (Object param : params) {
			if (param instanceof List<?>) {
				param = ((List<?>) param).toArray(new Object[((List<?>) param).size()]);
			}
			cacheKeys.add(new CacheKey(category, param));
		}

		return mGet(cacheKeys);
	}

	public <T> List<T> mGet(EntityKey... keys) throws Exception {
		if (keys == null || keys.length == 0) {
			return null;
		}
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		for (EntityKey eKey : keys) {
			String category = CacheAnnotationUtils.getCacheCategory(eKey.cz);
			cacheKeys.add(new CacheKey(category, eKey.params));
		}

		return mGet(cacheKeys);
	}

	public <T> Map<String, T> mGet(Set<String> keys) throws Exception {
		if (keys == null) {
			throw new IllegalArgumentException("Parameter keys is null.");
		}
		if (keys.isEmpty()) {
			return Collections.emptyMap();
		}

		Transaction t = Cat.getProducer().newTransaction("Cache." + defaultCacheType, defaultCategory + ":mGet");
		t.setStatus(Message.SUCCESS);
		String statusKey = "mget";
		StatusHolder.flowIn(defaultCacheType, defaultCategory, statusKey);

		long begin = System.nanoTime();
		try {
			CacheClient cacheClient = getCacheClient(defaultCacheType);
			KeyCountMonitor.getInstance().logKeyCount(defaultCacheType, defaultCategory, "mget", keys.size());
			Map<String, T> cachedDataMap = cacheClient.getBulk(keys, Object.class, false, null, false);
			long end = System.nanoTime();
			TimeMonitor.getInstance().logTime(defaultCacheType, defaultCategory, "mget", end - begin);

			int hits = cachedDataMap == null ? 0 : cachedDataMap.size();
			HitRateMonitor.getInstance().logHitRate(defaultCacheType, defaultCategory, "mget",
					(int) (100 * ((float) (hits) / keys.size())), hits);
			if (cachedDataMap != null && !cachedDataMap.isEmpty()) {
				if (enableTracker) {
					try {
						cacheTracker.addGetInfo("*[mget(" + keys.size() + ")-" + defaultCacheType + "]", end - begin);
					} catch (Throwable e) {
						CacheMonitorUtil.logCacheError("error with cache tracker", e);
					}
				}
			}
			t.addData("finalKeys",
					Stringizers.forJson().compact().from(keys, CatConstants.MAX_LENGTH, CatConstants.MAX_ITEM_LENGTH));
			return cachedDataMap;
		} catch (TimeoutException e) {
			Cat.getProducer().logEvent("Cache." + defaultCacheType, defaultCategory + ":timeout", Message.SUCCESS, "");
			t.addData("finalKeys",
					Stringizers.forJson().compact().from(keys, CatConstants.MAX_LENGTH, CatConstants.MAX_ITEM_LENGTH));
			t.setStatus(e);
			TimeMonitor.getInstance().logTime(defaultCacheType, defaultCategory, "mget", System.nanoTime() - begin,
					"timeout");
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new CacheException(e.getMessage(), e);
			}
		} finally {
			t.complete();
			StatusHolder.flowOut(defaultCacheType, defaultCategory, statusKey);
		}
	}

	public <T> List<T> mGet(CacheKey cacheKey) throws Exception {
		if (cacheKey == null) {
			throw new IllegalArgumentException("Parameter cacheKey is null.");
		}

		List<CacheKey> cacheKeys = get(cacheKey);
		if (cacheKeys == null || cacheKeys.isEmpty()) {
			return null;
		}

		return this.mGet(cacheKeys);
	}

	public <T> List<T> mGet(List<CacheKey> keys) throws Exception {
		return mGet(keys, true);
	}

	public <T> List<T> mGet(List<CacheKey> keys, boolean returnNullIfAnyKeyMissed) throws Exception {
		if (keys == null) {
			throw new IllegalArgumentException("Parameter keys is null.");
		}
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}
		final List<String> finalKeys = new ArrayList<String>(keys.size());
		Map<String, String> categories = null;

		CacheKeyType cacheKeyType = getCacheKeyMetadata(keys.get(0));
		String cacheType = cacheKeyType.getCacheType();
		String category = keys.get(0).getCategory();
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":mGet");
		}
		String statusKey = "mget";
		StatusHolder.flowIn(cacheType, category, statusKey);

		long begin = System.nanoTime();
		try {
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			if (!cacheClient.isDistributed()) {
				categories = new HashMap<String, String>();
			}
			for (CacheKey key : keys) {
				String finalKey = cacheKeyType.getKey(key.getParams());
				finalKeys.add(finalKey);
				if (!cacheClient.isDistributed()) {
					categories.put(finalKey, cacheKeyType.getCategory());
				}
			}
			KeyCountMonitor.getInstance().logKeyCount(cacheType, category, "mget", keys.size());

			Map<String, T> cachedDataMap = cacheClient.getBulk(finalKeys, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), categories, false);
			long end = System.nanoTime();
			TimeMonitor.getInstance().logTime(cacheType, category, "mget", end - begin);

			if (cachedDataMap == null)
				cachedDataMap = Collections.EMPTY_MAP;
			int hits = cachedDataMap.size();
			HitRateMonitor.getInstance().logHitRate(cacheType, category, "mget",
					(int) (100 * ((float) (hits) / keys.size())), hits);

			if (returnNullIfAnyKeyMissed) {
				boolean allGeted = (cachedDataMap.size() == keys.size());
				if (!allGeted) {
					Cat.getProducer().logEvent("Cache." + cacheType, category + ":missed", Message.SUCCESS, "");
					return null;
				}
			}
			if (t != null) {
				t.setStatus(Message.SUCCESS);
			}
			if (enableTracker) {
				try {
					cacheTracker.addGetInfo(
							cacheKeyType.getCategory() + "[mget(" + keys.size() + ")-" + cacheKeyType.getCacheType()
									+ "]", end - begin);
				} catch (Throwable e) {
					CacheMonitorUtil.logCacheError("error with cache tracker", e);
				}
			}
			// no cache expired, then reform result data by keys order
			if (cachedDataMap.isEmpty()) {
				return null;
			}
			List<T> sortedCachedData = new ArrayList<T>(keys.size());
			for (String finalKey : finalKeys) {
				sortedCachedData.add(cachedDataMap.get(finalKey));
			}
			return sortedCachedData;
		} catch (TimeoutException e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logEvent("Cache." + cacheType, category + ":timeout", Message.SUCCESS, "");
			TimeMonitor.getInstance().logTime(cacheType, category, "mget", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (Throwable e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logError(e);
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new CacheException(e.getMessage(), e);
			}
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public boolean delete(CacheKey key) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Cache key is null");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String finalKey = cacheKeyType.getKey(key.getParams());
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();
		boolean isHot = cacheKeyType.isHot();
		Object[] params = key.getParams();

		String categoryName = category == null ? defaultCategory : category;
		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, categoryName + ":delete");
		t.setStatus(Message.SUCCESS);
		long begin = System.nanoTime();
		try {
			CacheClient cacheClient = getCacheClient(cacheType);
			boolean isDistributed = cacheClient.isDistributed();
			boolean result = false;
			if (isDistributed) {
				result = cacheClient.delete(finalKey, isHot, category);
			}
			if (!isDistributed || (category != null && false)) {
				result = cacheClient.delete(finalKey, isHot, category);
				cacheManageWebService.clearByKey(new CacheClearDTO(cacheType, finalKey, category,
						params != null ? Arrays.asList(params) : new ArrayList<Object>()));
			}
			t.addData("finalKey", finalKey);
			TimeMonitor.getInstance().logTime(cacheType, category, "delete", System.nanoTime() - begin);
			return result;
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			TimeMonitor.getInstance().logTime(cacheType, category, "delete", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public Future<Boolean> asyncDelete(CacheKey key) throws CacheException {
		if (key == null) {
			throw new IllegalArgumentException("Cache key is null");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String finalKey = cacheKeyType.getKey(key.getParams());
		String cacheType = cacheKeyType.getCacheType();
		return asyncDelete(cacheType, finalKey, cacheKeyType.isHot(), key.getCategory(), key.getParams());
	}

	public Future<Boolean> asyncDelete(String cacheType, String key) throws CacheException {
		if (key == null) {
			throw new IllegalArgumentException("Parameter key is null.");
		}
		return asyncDelete(cacheType, key, false, null, null);
	}

	private Future<Boolean> asyncDelete(String cacheType, String key, boolean isHot, String category, Object[] params)
			throws CacheException {
		String categoryName = category == null ? defaultCategory : category;
		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, categoryName + ":asyncDelete");
		t.setStatus(Message.SUCCESS);
		try {
			CacheClient cacheClient = getCacheClient(cacheType);
			boolean isDistributed = cacheClient.isDistributed();
			Future<Boolean> future = null;
			if (isDistributed) {
				future = cacheClient.asyncDelete(key, isHot, category);
			}
			// 是否考虑可以直接通过swallow发送清除缓存和同步.net缓存的消息，而不需要经过cache-service?
			if (!isDistributed || (category != null && false)) {
				future = cacheClient.asyncDelete(key, isHot, category);
				cacheManageWebService.clearByKey(new CacheClearDTO(cacheType, key, category, params != null ? Arrays
						.asList(params) : new ArrayList<Object>()));
			}
			t.addData("finalKey", key);
			return future;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public String getFinalKey(CacheKey key) {
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		return cacheKeyType.getKey(key.getParams());
	}

	public CacheClient getCacheClient(String cacheType) {
		CacheClient cacheClient = cacheClientFactory.findCacheClient(cacheType);
		if (cacheClient == null) {
			throw new RuntimeException("No CacheClient found with type[" + cacheType + "].");
		}
		return cacheClient;
	}

	public CacheClient getCacheClient(CacheKey key) {
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		if (cacheKeyType == null) {
			throw new RuntimeException("No CacheClient found with category[" + key.getCategory() + "].");
		}
		return getCacheClient(cacheKeyType.getCacheType());
	}

	/**
	 * Retrieve {@link CacheKeyType} instance by {@link CacheKey} instance
	 */
	private CacheKeyType getCacheKeyMetadata(CacheKey key) {
		if (key == null) {
			throw new IllegalArgumentException("Cache key is null");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key.getCategory());
		if (cacheKeyType == null) {
			throw new RuntimeException("The cache item  for " + key.toString()
					+ " is not found in configuration files.");
		}
		return cacheKeyType;
	}

	/**
	 * Retrieve {@link CacheKeyType} object by category from metadata
	 */
	private CacheKeyType getCacheKeyMetadata(String category) {
		if (category == null) {
			throw new IllegalArgumentException("Cache category is null");
		}
		CacheKeyType cacheKeyType = configManager.findCacheKeyType(category);
		if (cacheKeyType == null) {
			throw new RuntimeException("Configuration not found with cache category[" + category + "].");
		}
		return cacheKeyType;
	}

	/**
	 * Generate the cache key for object. The object should be annotated by
	 * {@link Cache}
	 */
	private CacheKey getCacheKey(Object entity) {
		Class<?> cz = entity.getClass();

		Object[] cacheKeyValues = CacheAnnotationUtils.getCacheKeyValues(entity);

		Cache cache = cz.getAnnotation(Cache.class);

		if (cache == null) {
			throw new RuntimeException("No Cache Annotation found on class[" + cz.getName() + "].");
		}

		return new CacheKey(cache.category(), cacheKeyValues);
	}

	/**
	 * @return the defaultCacheType
	 */
	public String getDefaultCacheType() {
		return defaultCacheType;
	}

	/**
	 * @param defaultCacheType
	 *            the defaultCacheType to set
	 */
	public void setDefaultCacheType(String defaultCacheType) {
		this.defaultCacheType = defaultCacheType;
	}

	public <T> Map<CacheKey, T> mGetWithNonExists(List<CacheKey> keys) throws Exception {
		return mGetWithTimeoutAware(keys);
	}

	@SuppressWarnings("unchecked")
	public <T> Map<CacheKey, T> mGetWithTimeoutAware(List<CacheKey> keys) throws Exception {
		if (keys == null) {
			throw new IllegalArgumentException("Parameter keys is null.");
		}
		if (keys.isEmpty()) {
			return Collections.EMPTY_MAP;
		}
		final List<String> finalKeys = new ArrayList<String>(keys.size());
		final Map<String, CacheKey> finalKeyCacheKeyMapping = new HashMap<String, CacheKey>((int) (keys.size() * 1.5));
		Map<String, String> categories = null;

		CacheKeyType cacheKeyType = getCacheKeyMetadata(keys.get(0));
		String cacheType = cacheKeyType.getCacheType();
		String category = keys.get(0).getCategory();
		Transaction t = null;
		if (needLog(cacheType)) {
			t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":mGet");
			t.setStatus(Message.SUCCESS);
		}
		String statusKey = "mget";
		StatusHolder.flowIn(cacheType, category, statusKey);
		long begin = System.nanoTime();
		try {
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			if (!cacheClient.isDistributed()) {
				categories = new HashMap<String, String>();
			}

			for (CacheKey key : keys) {
				String finalKey = cacheKeyType.getKey(key.getParams());
				finalKeys.add(finalKey);
				finalKeyCacheKeyMapping.put(finalKey, key);
				if (!cacheClient.isDistributed()) {
					categories.put(finalKey, cacheKeyType.getCategory());
				}
			}
			KeyCountMonitor.getInstance().logKeyCount(cacheType, category, "mget", keys.size());

			Map<String, T> cachedDataMap = cacheClient.getBulk(finalKeys, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), categories, true);
			long end = System.nanoTime();
			TimeMonitor.getInstance().logTime(cacheType, category, "mget", end - begin);

			if (cachedDataMap == null)
				cachedDataMap = Collections.EMPTY_MAP;

			int hits = cachedDataMap.size();
			HitRateMonitor.getInstance().logHitRate(cacheType, category, "mget",
					(int) (100 * ((float) (hits) / keys.size())), hits);
			if (enableTracker) {
				try {
					cacheTracker.addGetInfo(
							cacheKeyType.getCategory() + "[mget(" + keys.size() + ")-" + cacheKeyType.getCacheType()
									+ "]", end - begin);
				} catch (Throwable e) {
					CacheMonitorUtil.logCacheError("error with cache tracker", e);
				}
			}

			Map<CacheKey, T> res = new HashMap<CacheKey, T>((int) (keys.size() * 1.5));
			for (String finalKey : finalKeys) {
				res.put(finalKeyCacheKeyMapping.get(finalKey), cachedDataMap.get(finalKey));
			}
			return res;
		} catch (TimeoutException e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logEvent("Cache." + cacheType, category + ":timeout", Message.SUCCESS, "");
			TimeMonitor.getInstance().logTime(cacheType, category, "mget", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (Throwable e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logError(e);
			if (e instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new CacheException(e.getMessage(), e);
			}
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}

	public long increment(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":inc");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.increment(finalKey, amount, cacheKeyType.getCategory(), defaultValue);
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public long increment(CacheKey key, int amount) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":inc");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.increment(finalKey, amount, cacheKeyType.getCategory());
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public long decrement(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":dec");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.decrement(finalKey, amount, cacheKeyType.getCategory(), defaultValue);
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public long decrement(CacheKey key, int amount) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":dec");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.decrement(finalKey, amount, cacheKeyType.getCategory());
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public <T> CASValue<T> gets(CacheKey key) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":gets");
		t.setStatus(Message.SUCCESS);
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			return cacheClient.gets(finalKey, cacheKeyType.getCategory());
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public CASResponse cas(CacheKey key, long casId, Object value) throws CacheException, TimeoutException {
		if (key == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":cas");
		t.setStatus(Message.SUCCESS);
		long begin = System.nanoTime();
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			CASResponse resp = cacheClient.cas(finalKey, casId, value, cacheKeyType.getCategory());
			TimeMonitor.getInstance().logTime(cacheType, category, "cas", System.nanoTime() - begin);
			return resp;
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			TimeMonitor.getInstance().logTime(cacheType, category, "cas", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
		}
	}

	public boolean set(CacheKey key, Object value) throws CacheException, TimeoutException {
		return set(key, value, -1);
	}

	public boolean set(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getCacheKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();

		Transaction t = Cat.getProducer().newTransaction("Cache." + cacheType, category + ":set");
		t.setStatus(Message.SUCCESS);
		String statusKey = "set";
		StatusHolder.flowIn(cacheType, category, statusKey);
		long begin = System.nanoTime();
		try {
			String finalKey = cacheKeyType.getKey(key.getParams());
			CacheClient cacheClient = getCacheClient(cacheKeyType.getCacheType());
			t.addData("finalKey", finalKey);
			boolean ret = false;
			if (timeout <= 0) {
				ret = cacheClient.set(finalKey, value, cacheKeyType.getDurationSeconds(), cacheKeyType.isHot(),
						cacheKeyType.getCategory());
			} else {
				ret = cacheClient.set(finalKey, value, cacheKeyType.getDurationSeconds(), timeout,
						cacheKeyType.isHot(), cacheKeyType.getCategory());
			}
			TimeMonitor.getInstance().logTime(cacheType, category, "set", System.nanoTime() - begin);
			return ret;
		} catch (TimeoutException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			TimeMonitor.getInstance().logTime(cacheType, category, "set", System.nanoTime() - begin, "timeout");
			throw e;
		} catch (CacheException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new CacheException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
}
