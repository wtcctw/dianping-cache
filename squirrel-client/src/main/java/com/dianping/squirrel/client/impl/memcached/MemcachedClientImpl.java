/**
 * Project: avatar-cache
 * 
 * File Created at 2010-7-12 $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited. All rights reserved.
 * 
 * This software is the confidential and proprietary information of Dianping
 * Company. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Dianping.com.
 */
package com.dianping.squirrel.client.impl.memcached;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetCompletionListener;
import net.spy.memcached.internal.BulkGetFuture;
import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.GetCompletionListener;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.squirrel.client.core.CASResponse;
import com.dianping.squirrel.client.core.CASValue;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.CacheClient;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreTypeAware;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreInitializeException;
import com.dianping.squirrel.common.util.CacheKeyUtils;
import com.dianping.squirrel.common.util.RetryLoop;
import com.dianping.squirrel.common.util.RetryLoop.RetryResponse;

/**
 * The memcached client implementation adaptor(sypmemcached)
 * 
 * @author guoqing.chen
 * @author danson.liu
 * @author enlight.chen
 * @author xiang.wu
 * 
 */
public class MemcachedClientImpl implements CacheClient, Lifecycle, StoreTypeAware {

	/**
	 * in milliseconds
	 */
	private static final long DEFAULT_GET_TIMEOUT = 50;

	private static final long DEFAULT_MGET_TIMEOUT = 80;

	private static final long DEFAULT_ADD_TIMEOUT = 50;

	private static final long DEFAULT_SET_TIMEOUT = 50;

	private static final long DEFAULT_OP_QUEUE_MAX_BLOCK_TIME = 10;

	private static final String PROP_OP_QUEUE_LEN = "avatar-cache.spymemcached.queuesize";

	private static final String PROP_READ_BUF_SIZE = "avatar-cache.spymemcached.readbufsize";

	private static final String PROP_OP_QUEUE_MAX_BLOCK_TIME = "avatar-cache.spymemcached.queueblocktime";

	private static final String KEY_GET_TIMEOUT = "avatar-cache.memcached.get.timeout";

	private static final String KEY_MGET_TIMEOUT = "avatar-cache.memcached.mget.timeout";

	private static final String KEY_ADD_TIMEOUT = "avatar-cache.memcached.add.timeout";

	private static final String KEY_HOTKEY_LOCKTIME = "avatar-cache.memcached.hotkey.locktime";

	private static final String MSG_SUCCESS = Message.SUCCESS;

	private static final String MSG_TIMEOUT = "-1";

	private static final String MSG_EXCEPTION = "-2";

	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static int opQueueLen = configManager.getIntValue(PROP_OP_QUEUE_LEN,
			DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN);

	private static int readBufSize = configManager.getIntValue(PROP_READ_BUF_SIZE,
			DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE);

	private static long opQueueMaxBlockTime = configManager.getLongValue(PROP_OP_QUEUE_MAX_BLOCK_TIME,
			DEFAULT_OP_QUEUE_MAX_BLOCK_TIME);

	private static long timeoutMGet = configManager.getLongValue(KEY_MGET_TIMEOUT, DEFAULT_MGET_TIMEOUT);

	private static long timeoutGet = configManager.getLongValue(KEY_GET_TIMEOUT, DEFAULT_GET_TIMEOUT);

	private static long timeoutAdd = configManager.getLongValue(KEY_ADD_TIMEOUT, DEFAULT_ADD_TIMEOUT);

	private static long timeoutSet = configManager.getLongValue(KEY_ADD_TIMEOUT, DEFAULT_SET_TIMEOUT);

	private static int hotkeyLockTime = configManager.getIntValue(KEY_HOTKEY_LOCKTIME, 30);

	private static Logger logger = LoggerFactory.getLogger(MemcachedClientImpl.class);

	private static final boolean enableClear = configManager.getBooleanValue("avatar-cache.memcached.clear.enable",
			false);

	private static final String KEY_HOTKEY_EXPIRATION = "avatar-cache.memcached.hotkey.expiration";

	private static int hotKeyExpiration = configManager.getIntValue(KEY_HOTKEY_EXPIRATION, 10);

	private static final String KEY_HOTKEY_RECENTSECONDS = "avatar-cache.memcached.hotkey.recentseconds";

	private static int hotKeyRecentSeconds = configManager.getIntValue(KEY_HOTKEY_RECENTSECONDS, 30);

	private static final String KEY_HOTKEY_HITRANGE = "avatar-cache.memcached.hotkey.hitrange";

	private static int hotKeyHitRange = configManager.getIntValue(KEY_HOTKEY_HITRANGE, 10000);

	private static final String KEY_GET_TIMEOUT_LIST = "avatar-cache.memcached.get.timeoutlist";

	private static final String KEY_HOTKEY_REMOVE_BACKUP = "avatar-cache.memcached.hotkey.removebackup";

	private static boolean removeBackup = configManager.getBooleanValue(KEY_HOTKEY_REMOVE_BACKUP, true);

	/**
	 * Memcached client unique key
	 */
	private String key;

	/**
	 * Memcached client
	 */
	private MemcachedClient readClient;

	private MemcachedClient writeClient;

	/**
	 * Spymemcached client configuration
	 */
	private MemcachedClientConfiguration config;

	private static final int POOLSIZE_READ = configManager.getIntValue("avatar-cache.spymemcached.poolsize.read", 3) > 0 ? configManager
			.getIntValue("avatar-cache.spymemcached.poolsize.read", 3) : 3;

	private static final int POOLSIZE_WRITE = configManager.getIntValue("avatar-cache.spymemcached.poolsize.write", 1) > 0 ? configManager
			.getIntValue("avatar-cache.spymemcached.poolsize.write", 1) : 1;

	private static final boolean USE_SHARED_POOL = configManager.getBooleanValue(
			"avatar-cache.spymemcached.pool.shared", false);

	private MemcachedClient[] readClients;

	private MemcachedClient[] writeClients;

	static {
		try {
			configManager.registerConfigChangeListener(new ConfigChangeHandler());
			String timeoutConfig = configManager.getStringValue(KEY_GET_TIMEOUT_LIST, "50");
			RetryLoop.initTimeoutConfig(timeoutConfig);
		} catch (Exception e) {
		}
	}

	@Override
	public String getStoreType() {
		return key;
	}

	@Override
	public void setStoreType(String key) {
		this.key = key;
	}

	@Override
	public void initialize(StoreClientConfig config) {
		this.config = (MemcachedClientConfiguration) config;
		NodeMonitor.getInstance().clear(key);
	}

	@Override
	public boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		String k = getCacheKey(key, isHot);
		Object v = getCacheValue(value, expiration, isHot);
		MemcachedClient client = getWriteClient();
		try {
			OperationFuture<Boolean> future = client.add(k, expiration, v);
			if (future != null) {
				boolean result = future.get(timeout, TimeUnit.MILLISECONDS);
				if (result) {
					asyncSetBackupKey(key, value, expiration, isHot, category);
				}
				NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
				return result;
			}
		} catch(CheckedOperationTimeoutException e) {
		    NodeMonitor.getInstance().logNode(client, k, MSG_TIMEOUT, category);
            throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
		NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
		return false;
	}

	@Override
	public boolean add(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return add(key, value, expiration, timeoutAdd, isHot, category);
	}

	@Override
	public Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		String k = getCacheKey(key, isHot);
		Object v = getCacheValue(value, expiration, isHot);
		MemcachedClient client = getWriteClient();
		try {
			Future<Boolean> future = client.add(k, expiration, v);
			NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
			return future;
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public <T> CASValue<T> gets(String key, String category) throws StoreException, TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getReadClient();
		try {
			net.spy.memcached.CASValue<T> v = (net.spy.memcached.CASValue<T>) client.gets(reformedKey);
			CASValue<T> casValue = null;
			if (v != null) {
				casValue = new CASValue<T>(v.getCas(), v.getValue());
			}
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return casValue;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public CASResponse cas(String key, long casId, Object value, String category) throws StoreException,
			TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getWriteClient();
		try {
			net.spy.memcached.CASResponse r = client.cas(reformedKey, casId, value);
			CASResponse casResponse = null;
			if (r != null) {
				casResponse = CASResponse.valueOf(r.name());
			}
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return casResponse;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public <T> Map<String, T> getBulk(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			boolean timeoutAware) throws Exception {
		keys = CacheKeyUtils.reformKeys(keys);
		MemcachedClient client = getReadClient();
		Map<String, T> result = null;
		TimeoutException timeoutException = null;
		try {
			// use timeout to eliminate memcached servers' crash
			result = (Map<String, T>) doGetBulk(client, keys, timeoutMGet);
			NodeMonitor.getInstance().logNode(client, keys, MSG_SUCCESS, "");
		} catch (TimeoutException e) {
			timeoutException = e;
			result = null;
			NodeMonitor.getInstance().logNode(client, keys, MSG_TIMEOUT, "");
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, keys, MSG_EXCEPTION, "");
			throw e;
		}
		if (timeoutAware && timeoutException != null && result == null) {
			throw timeoutException;
		}
		if (result == null || result.isEmpty()) {
			return result;
		}
		return CacheKeyUtils.reformBackKeys(result);
	}

	@Override
	public Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws StoreException {
		String reformedKey = getCacheKey(key, isHot);
		Future<Boolean> future = getWriteClient().delete(reformedKey);
		asyncRemoveBackupKey(key, isHot, category);
		return future;
	}

	public boolean delete(String key, boolean isHot, String category) throws StoreException, TimeoutException {
		return this.delete(key, isHot, category, timeoutSet);
	}

	public boolean delete(String key, boolean isHot, String category, long timeout) throws StoreException,
			TimeoutException {
		MemcachedClient client = getWriteClient();
		try {
			String reformedKey = getCacheKey(key, isHot);
			Future<Boolean> future = client.delete(reformedKey);
			if (future != null) {
				boolean result = future.get(timeout, TimeUnit.MILLISECONDS);
				if (result) {
					asyncRemoveBackupKey(key, isHot, category);
				}
				NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, category);
				return result;
			}
			return false;
		} catch (CheckedOperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public void replace(String key, Object value, int expiration, boolean isHot, String category) throws Exception {
		String reformedKey = getCacheKey(key, isHot);
		getWriteClient().replace(reformedKey, expiration, value);
	}

	@Override
	public Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		String k = getCacheKey(key, isHot);
		Object v = getCacheValue(value, expiration, isHot);
		MemcachedClient client = getWriteClient();
		try {
			Future<Boolean> future = client.set(k, expiration, v);
			asyncSetBackupKey(k, value, expiration, isHot, category);
			NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
			return future;
		} catch (RuntimeException e) {
			NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public long decrement(String key, int amount, String category) throws StoreException, TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getWriteClient();
		try {
			long value = client.decr(reformedKey, amount);
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return value;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public long decrement(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getWriteClient();
		try {
			long value = client.decr(reformedKey, amount, def);
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return value;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public long increment(String key, int amount, String category) throws StoreException, TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getWriteClient();
		try {
			long value = client.incr(reformedKey, amount);
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return value;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public long increment(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		String reformedKey = CacheKeyUtils.reformKey(key);
		MemcachedClient client = getWriteClient();
		try {
			long value = client.incr(reformedKey, amount, def);
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, category);
			return value;
		} catch (OperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
	}

	@Override
	public void clear() {
		if (enableClear) {
			if (writeClient != null) {
				writeClient.flush();
			}
			if (writeClients != null) {
				for (MemcachedClient client : writeClients) {
					client.flush();
				}
			}
		}
	}

	@Override
	public void stop() {
		if (readClient != null) {
			readClient.shutdown();
		}
		if (writeClient != null) {
			writeClient.shutdown();
		}
		if (readClients != null) {
			for (MemcachedClient client : readClients) {
				client.shutdown();
			}
		}
		if (writeClients != null) {
			for (MemcachedClient client : writeClients) {
				client.shutdown();
			}
		}
	}

	public MemcachedClient getReadClient() {
		if (this.readClient != null)
			return this.readClient;
		try {
			int idx = (int) (Math.random() * POOLSIZE_READ);
			return readClients[idx];
		} catch (RuntimeException e) {
		}
		return readClients[0];
	}

	public MemcachedClient getWriteClient() {
		if (this.writeClient != null)
			return this.writeClient;
		try {
			int idx = (int) (Math.random() * POOLSIZE_WRITE);
			return writeClients[idx];
		} catch (RuntimeException e) {
		}
		return writeClients[0];
	}

	@Override
	public void start() {
		try {
			// use ketama to provide consistent node hashing
			ExtendedConnectionFactory connectionFactory = new ExtendedKetamaConnectionFactory(opQueueLen, readBufSize,
					opQueueMaxBlockTime);
			if (config.getTranscoder() != null) {
				if (config.getTranscoder() instanceof HessianTranscoder) {
					((HessianTranscoder) config.getTranscoder()).setCacheType(this.getStoreType());
				}
				connectionFactory.setTranscoder(config.getTranscoder());
			} else {
				// set transcoder to HessianTranscoder:
				// 1. fast
				// 2. Fixed bug in https://bugs.launchpad.net/play/+bug/503349
				connectionFactory.setTranscoder(new HessianTranscoder(this.getStoreType()));
			}
			String servers = config.getServers();
			if (servers == null) {
				throw new RuntimeException("Server address must be specified.");
			}
			readClients = new MemcachedClient[POOLSIZE_READ];
			writeClients = new MemcachedClient[POOLSIZE_WRITE];
			if (!servers.contains(",")) {
				// memcached
				String[] serverSplits = config.getServers().split("\\|");
				String mainServer = serverSplits[0].trim();
				if (USE_SHARED_POOL) {
					MemcachedClient client = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(mainServer));
					this.readClient = client;
					this.writeClient = client;
				} else {
					for (int i = 0; i < POOLSIZE_READ; i++) {
						MemcachedClient client = new MemcachedClient(connectionFactory,
								AddrUtil.getAddresses(mainServer));
						readClients[i] = client;
					}
					for (int i = 0; i < POOLSIZE_WRITE; i++) {
						MemcachedClient client = new MemcachedClient(connectionFactory,
								AddrUtil.getAddresses(mainServer));
						writeClients[i] = client;
					}
				}
			} else {
				// kvdb
				String[] serverSplits = servers.split(" ");
				String writeServer = serverSplits[0].trim();
				String readServers = serverSplits.length == 1 ? writeServer : serverSplits[1].trim();
				readClient = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(readServers));
				writeClient = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(writeServer));
			}
			try {
				NodeMonitor.getInstance().addNodes(this.key, getMemcachedClient());
			} catch (Exception e) {
				logger.warn("", e);
			}
			logger.info("spymemcached client started!");
		} catch (Exception e) {
			throw new StoreInitializeException(e);
		}
	}

	private static class ConfigChangeHandler implements ConfigChangeListener {

		@Override
		public void onChange(String key, String value) {
			if (key.endsWith(KEY_MGET_TIMEOUT)) {
				timeoutMGet = Long.valueOf(value);
			} else if (key.endsWith(KEY_GET_TIMEOUT)) {
				timeoutGet = Integer.valueOf(value);
			} else if (key.endsWith(KEY_GET_TIMEOUT_LIST)) {
				RetryLoop.initTimeoutConfig(value);
			} else if (key.endsWith(KEY_ADD_TIMEOUT)) {
				long timeout = Long.valueOf(value);
				timeoutAdd = timeout;
				timeoutSet = timeout;
			} else if (key.endsWith(KEY_HOTKEY_LOCKTIME)) {
				hotkeyLockTime = Integer.valueOf(value);
			} else if (key.endsWith(KEY_HOTKEY_EXPIRATION)) {
				hotKeyExpiration = Integer.valueOf(value);
			} else if (key.endsWith(KEY_HOTKEY_RECENTSECONDS)) {
				hotKeyRecentSeconds = Integer.valueOf(value);
			} else if (key.endsWith(KEY_HOTKEY_HITRANGE)) {
				hotKeyHitRange = Integer.valueOf(value);
			} else if (key.endsWith(KEY_HOTKEY_REMOVE_BACKUP)) {
				removeBackup = Boolean.valueOf(value);
			}
		}

	}

	Collection<MemcachedClient> getMemcachedClient() {
		Collection<MemcachedClient> memcachedClients = new HashSet<MemcachedClient>();
		if (readClient != null) {
			memcachedClients.add(readClient);
		}
		if (writeClient != null) {
			memcachedClients.add(writeClient);
		}
		if (readClients != null) {
			for (MemcachedClient client : readClients) {
				memcachedClients.add(client);
			}
		}
		if (writeClients != null) {
			for (MemcachedClient client : writeClients) {
				memcachedClients.add(client);
			}
		}
		return memcachedClients;
	}

	@Override
	public boolean isDistributed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cache.core.CacheClient#get(java.lang.String,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, Class dataType, String category) throws Exception {
		try {
			return (T) get(key, Object.class, false, category, false);
		} catch (TimeoutException e) {
			return null;
		}
	}

	@Override
	public boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		String k = getCacheKey(key, isHot);
		Object v = getCacheValue(value, expiration, isHot);
		MemcachedClient client = getWriteClient();
		try {
			OperationFuture<Boolean> future = client.set(k, expiration, v);
			if (future != null) {
				boolean success = future.get(timeout, TimeUnit.MILLISECONDS);
				if (success) {
					asyncSetBackupKey(k, value, expiration, isHot, category);
				}
				NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
				return success;
			}
		} catch (CheckedOperationTimeoutException e) {
			NodeMonitor.getInstance().logNode(client, k, MSG_TIMEOUT, category);
			throw new TimeoutException(e.getMessage());
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
		NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
		return false;
	}

	@Override
	public boolean set(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return set(key, value, expiration, timeoutSet, isHot, category);
	}

	private String getCacheKey(String key, boolean isHot) {
		return CacheKeyUtils.reformKey(key);
	}

	private Object getCacheValue(Object value, int expiration, boolean isHot) {
		return value;
	}

	public void asyncSetBackupKey(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		if (isHot) {
			MemcachedClient client = getWriteClient();
			try {
				client.set(CacheKeyUtils.reformKey(key, true), expiration + hotKeyExpiration, value);
			} catch (RuntimeException e) {
				Cat.logEvent("Cache." + this.getStoreType(), category + ":setBackupFail", "-1",
						"key=" + key + "&error=" + e.getMessage());
			}
		}
	}

	public void asyncRemoveBackupKey(String key, boolean isHot, String category) throws StoreException {
		if (isHot && removeBackup) {
			try {
				getWriteClient().delete(CacheKeyUtils.reformKey(key, true));
			} catch (RuntimeException e) {
				Cat.logEvent("Cache." + this.getStoreType(), category + ":removeBackupFail", "-1", "key=" + key + "&error="
						+ e.getMessage());
			}
		}
	}

	private Object doGet(String key, String category) throws Exception {
		Object result = null;
		TimeoutException timeoutException = null;
		MemcachedClient client = getReadClient();
		RetryResponse retryResponse = null;
		try {
			retryResponse = RetryLoop.hedgedGet(client, key);
			result = retryResponse.getResult();
			NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, category);
		} catch (TimeoutException e) {
			timeoutException = e;
			result = null;
			NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, category);
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, category);
			throw e;
		}
		if (timeoutException != null && result == null) {
			throw timeoutException;
		}
		return result;
	}

	private Object getLastValue(String key, boolean isHot, String category) throws Exception {
		Object result = null;
		String lockKey = CacheKeyUtils.getLockKey(key);
		Future<Boolean> future = getWriteClient().add(lockKey, hotkeyLockTime, true);
		Boolean locked = null;
		try {
			locked = future.get(timeoutGet, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			future.cancel(true);
		}
		String lastVersionCacheKey = CacheKeyUtils.getLastVersionCacheKey(key);
		if (locked == null || !locked.booleanValue()) {
			try {
				result = doGet(lastVersionCacheKey, category);
				if (result != null) {
					Cat.logEvent("Cache." + this.getStoreType(), category + ":getLast", "0", lastVersionCacheKey);
				} else {
					Cat.logEvent("Cache." + this.getStoreType(), category + ":getLastMissed", "-1", lastVersionCacheKey);
				}
			} catch (TimeoutException e) {
				Cat.logEvent("Cache." + this.getStoreType(), category + ":getLastTimeout", "-1", lastVersionCacheKey);
				logger.error("memcached get last key {} timeout", lastVersionCacheKey);
				throw e;
			}
		} else {
			Cat.logEvent("Cache." + this.getStoreType(), category + ":lockAfterClear", "0", key);
			logger.info("memcached locked {} after clear category", lockKey);
			result = null;
		}
		return result;
	}

	public Object getBackupValue(String key, boolean isHot, String category) throws Exception {
		Object result = null;
		String lockKey = CacheKeyUtils.getLockKey(key);
		Future<Boolean> future = getWriteClient().add(lockKey, hotkeyLockTime, true);
		Boolean locked = null;
		try {
			locked = future.get(timeoutGet, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			future.cancel(true);
		}
		if (locked == null || !locked.booleanValue()) {
			String hotKey = CacheKeyUtils.reformKey(key, true);
			try {
				result = doGet(hotKey, category);
				if (result != null) {
					Cat.logEvent("Cache." + this.getStoreType(), category + ":getHot", "0", hotKey);
				} else {
					Cat.logEvent("Cache." + this.getStoreType(), category + ":getHotMissed", "-1", hotKey);
				}
			} catch (TimeoutException e) {
				Cat.logEvent("Cache." + this.getStoreType(), category + ":getHotTimeout", "-1", hotKey);
				logger.error("memcached get hot key {} timeout", hotKey);
				throw e;
			}
		} else {
			Cat.logEvent("Cache." + this.getStoreType(), category + ":lockAfterExp", "0", key);
			logger.info("memcached locked {} after expiration", lockKey);
			result = null;
		}
		return result;
	}

	@Override
	public <T> T get(String key, Class dataType, boolean isHot, String category, boolean timeoutAware) throws Exception {
		TimeoutException timeoutException = null;
		Object result = null;
		String finalKey = CacheKeyUtils.nextCacheKey(key, isHot, hotKeyHitRange);
		try {
			result = doGet(finalKey, category);
		} catch (TimeoutException e) {
			logger.error("memcached get {} timeout", key);
			timeoutException = e;
			result = null;
		}
		if (isHot) {
			if (result == null && timeoutException == null) {
				if (finalKey.endsWith(CacheKeyUtils.SUFFIX_HOT)) {
					try {
						result = doGet(getCacheKey(key, isHot), category);
					} catch (TimeoutException e) {
						timeoutException = e;
						result = null;
					}
				}
			}
			if (result == null && timeoutException == null) {
				if (this.config.isVersionChanged(category, hotKeyRecentSeconds)) {
					try {
						result = getLastValue(key, isHot, category);
					} catch (TimeoutException e) {
						timeoutException = e;
						result = null;
					}
				} else {
					try {
						result = getBackupValue(key, isHot, category);
					} catch (TimeoutException e) {
						timeoutException = e;
						result = null;
					}
				}
			}
		}
		if (timeoutAware && timeoutException != null && result == null) {
			throw timeoutException;
		}
		return (T) result;
	}

	private <T> Map<String, Object> doGetBulk(MemcachedClient client, Collection<String> key, long timeout)
			throws Exception {
		Map<String, Object> result = null;
		Future<Map<String, Object>> future = null;
		try {
			future = client.asyncGetBulk(key);
			if (future != null) {
				result = future.get(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			if (future != null) {
				future.cancel(true);
			}
			if (e instanceof CheckedOperationTimeoutException) {
				e = new TimeoutException(e.getMessage());
				throw e;
			} else if (e instanceof IllegalStateException) {
				e = new TimeoutException(e.getMessage());
				throw e;
			} else {
				throw e;
			}
		}
		return result;
	}

	@Override
	public void asyncSet(String key, Object value, int expiration, boolean isHot, String category,
	                     StoreCallback<Boolean> callback) {
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
	                     StoreCallback<Boolean> callback) {
		try {
			asyncAdd(key, value, expiration, isHot, category);
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
	public <T> Future<T> asyncGet(String key, Class dataType, boolean isHot, String category) throws StoreException {
		String finalKey = CacheKeyUtils.nextCacheKey(key, isHot, hotKeyHitRange);
		Future<T> future = null;
		MemcachedClient client = getReadClient();
		try {
			future = (Future<T>) client.asyncGet(finalKey);
		} catch (Exception e) {
			NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, category);
			throw new StoreException(e);
		}
		return future;
	}

	@Override
	public <T> void asyncGet(final String key, final Class dataType, final boolean isHot, final String category, final StoreCallback<T> callback) {
	    String finalKey = CacheKeyUtils.nextCacheKey(key, isHot, hotKeyHitRange);
        GetFuture<T> future = null;
        final MemcachedClient client = getReadClient();
        
        future = (GetFuture<T>) client.asyncGet(finalKey);
        future.addListener(new GetCompletionListener() {

            @Override
            public void onComplete(GetFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if(status.isSuccess() || status.getStatusCode() == StatusCode.ERR_NOT_FOUND) {
                    T result = (T) future.get();
                    NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, category);
                    callback.onSuccess(result);
                } else {
                    if(status.getStatusCode() == StatusCode.TIMEDOUT) {
                        NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, category);
                    } else {
                        NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, category);
                    }
                    callback.onFailure("get " + key + " " + status.getMessage(), null);
                }
            }
            
        });
	}

	@Override
	public <T> void asyncBatchGet(final Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			final StoreCallback<Map<String, T>> callback) {

        MemcachedClient client = getReadClient();
        
        BulkFuture<Map<String, Object>> future = client.asyncGetBulk(keys);
        future.addListener(new BulkGetCompletionListener() {
            
            @Override
            public void onComplete(BulkGetFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if(status.isSuccess() || status.getStatusCode() == StatusCode.ERR_NOT_FOUND) {
                    Map<String, T> result = (Map<String, T>) future.get();
                    callback.onSuccess(result);
                } else {
                    callback.onFailure("mget " + keyList(keys) + "failed: " + status.getMessage(), null);
                }
            }
        });
	}
	
	private String keyList(Collection<String> keys) {
	    int i=0;
	    StringBuilder buf = new StringBuilder(128);
	    for(String key : keys) {
	        buf.append(key);
	        if(++i < 5)
	            buf.append(',');
	        else 
	            buf.append("...");
	    }
	    return buf.toString();
	}

    @Override
    public <T> void asyncBatchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category,
                                  StoreCallback<Boolean> callback) {
        throw new UnsupportedOperationException("spymemcached doesn't support async batch set");
    }
    
    @Override
    public <T> boolean batchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category) 
    throws TimeoutException, StoreException {
        throw new UnsupportedOperationException("spymemcached doesn't support batch set");
    }
    
}
