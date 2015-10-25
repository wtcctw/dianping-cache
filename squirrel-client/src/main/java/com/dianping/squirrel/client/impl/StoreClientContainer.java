package com.dianping.squirrel.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.remote.cache.CacheManageWebService;
import com.dianping.remote.cache.dto.CacheClearDTO;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.CacheClientFactory;
import com.dianping.squirrel.client.config.CacheItemConfigManager;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.RemoteCacheClientFactory;
import com.dianping.squirrel.client.config.RemoteCacheItemConfigManager;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.client.monitor.TimeMonitor;
import com.dianping.squirrel.client.util.CacheMonitorUtil;
import com.dianping.squirrel.client.util.CacheTracker;
import com.dianping.squirrel.client.util.DefaultCacheTracker;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreTimeoutException;

public class StoreClientContainer {
	
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

	public StoreClientContainer() {
        configManager = RemoteCacheItemConfigManager.getInstance();

        cacheClientFactory = RemoteCacheClientFactory.getInstance();

        InvokerConfig<CacheManageWebService> config = new InvokerConfig<CacheManageWebService>(
                "http://service.dianping.com/cacheService/cacheManageService_1.0.0", CacheManageWebService.class);
        config.setCallType("oneway");
        config.setTimeout(10000);
        cacheManageWebService = ServiceFactory.getService(config);
	}
	
	public <T> T get(StoreKey key) throws StoreException {
		if (key == null) {
			throw new IllegalArgumentException("store key is null");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
		String cacheType = cacheKeyType.getCacheType();
		String category = key.getCategory();
		String finalKey = cacheKeyType.getKey(key.getParams());

		return get(cacheType, category, cacheKeyType.getDataTypeClass(), cacheKeyType.isHot(), finalKey);
	}
	
	public boolean set(StoreKey key, Object value) throws StoreException {
		return set(key, value, -1);
	}
	
	public boolean set(StoreKey key, Object value, long timeout) throws StoreException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("Key/Value is null.");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
			throw new StoreTimeoutException(e);
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
	
	public boolean addIfAbsent(StoreKey key, Object value) throws StoreException {
		return addIfAbsent(key, value, -1);
	}

	public boolean addIfAbsent(StoreKey key, Object value, long timeout) throws StoreException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key/value is null");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
			throw new StoreTimeoutException(e);
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
	
	public boolean delete(StoreKey key) throws StoreException {
		if (key == null) {
			throw new IllegalArgumentException("store key is null");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
			throw new StoreTimeoutException(e);
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			t.complete();
		}
	}
	
	private <T> T get(String cacheType, String category, Class dataType, boolean isHot, String finalKey) throws StoreException {
		if (finalKey == null) {
			throw new IllegalArgumentException("final key is null");
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
			throw new StoreTimeoutException(e);
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			if (t != null) {
				t.setStatus(e);
			}
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
	
	public <T> Future<T> asyncGet(StoreKey key) throws StoreException {
		if (key == null) {
			throw new IllegalArgumentException("store key is null");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			if (t != null) {
				t.complete();
			}
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
	
	public Future<Boolean> asyncSet(final StoreKey key, final Object value) throws StoreException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key/value is null.");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			t.complete();
			StatusHolder.flowOut(cacheType, category, statusKey);
		}
	}
	
	public Future<Boolean> asyncAdd(final StoreKey key, final Object value) throws StoreException {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key/value is null.");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
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
		} catch(StoreException e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw e;
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			throw new StoreException(e);
		} finally {
			t.complete();
		}
	}
	
	private boolean needLog(String cacheType) {
		if (!enableLogWeb && "web".equalsIgnoreCase(cacheType)) {
			return false;
		}
		return true;
	}
	
	public CacheClient getCacheClient(String cacheType) {
		CacheClient cacheClient = cacheClientFactory.findCacheClient(cacheType);
		if (cacheClient == null) {
			throw new RuntimeException("store client not found: " + cacheType);
		}
		return cacheClient;
	}

	public CacheClient getCacheClient(StoreKey key) {
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key);
		if (cacheKeyType == null) {
			if (cacheKeyType == null) {
				throw new RuntimeException("category config not found: " + key.getCategory());
			}
		}
		return getCacheClient(cacheKeyType.getCacheType());
	}
	
	/**
	 * Retrieve {@link CacheKeyType} instance by {@link StoreKey} instance
	 */
	private CacheKeyType getStoreKeyMetadata(StoreKey key) {
		if (key == null) {
			throw new IllegalArgumentException("store key is null");
		}
		CacheKeyType cacheKeyType = getStoreKeyMetadata(key.getCategory());
		return cacheKeyType;
	}
	
	/**
	 * Retrieve {@link CacheKeyType} object by category from meta data
	 */
	private CacheKeyType getStoreKeyMetadata(String category) {
		if (category == null) {
			throw new IllegalArgumentException("cache category is null");
		}
		CacheKeyType cacheKeyType = configManager.findCacheKeyType(category);
		return cacheKeyType;
	}


}
