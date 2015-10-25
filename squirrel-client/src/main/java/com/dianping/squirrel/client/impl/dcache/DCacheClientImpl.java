package com.dianping.squirrel.client.impl.dcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.core.CASResponse;
import com.dianping.squirrel.client.core.CASValue;
import com.dianping.squirrel.client.core.CacheCallback;
import com.dianping.squirrel.client.core.CacheClientConfiguration;
import com.dianping.squirrel.client.core.CacheFuture;
import com.dianping.squirrel.client.core.KeyAware;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreInitializeException;
import com.dianping.squirrel.common.util.CacheKeyUtils;
import com.qq.cloud.component.dcache.client.api.ClientCallback;
import com.qq.cloud.component.dcache.client.api.ClientFactory;
import com.qq.cloud.component.dcache.client.api.ConnectionConf;
import com.qq.cloud.component.dcache.client.api.DCacheClientAPI;
import com.qq.cloud.component.dcache.client.dcache.DCacheConst;
import com.qq.cloud.component.dcache.client.kv.BatchKVCacheResult;
import com.qq.cloud.component.dcache.client.kv.CacheValue;
import com.qq.cloud.component.dcache.client.kv.DCacheKVClientAPI;
import com.qq.cloud.component.dcache.client.kv.InputValue;
import com.qq.cloud.component.dcache.client.kv.KVCacheResult;

public class DCacheClientImpl implements DCacheClient, Lifecycle, KeyAware {

	private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	private static final String KEY_READ_TIMEOUT = "avatar-cache.dcache.timeout.read";
	private static final String KEY_CONNECT_TIMEOUT = "avatar-cache.dcache.timeout.connect";
	private static final String KEY_CONNECTIONS = "avatar-cache.dcache.connections";
	private static final String KEY_CALLBACK_QUEUESIZE = "avatar-cache.dcache.callback.queuesize";
	private static final String KEY_CALLBACK_CORESIZE = "avatar-cache.dcache.callback.coresize";
	private static final String KEY_CALLBACK_MAXSIZE = "avatar-cache.dcache.callback.maxsize";

	private static int readTimeout = configManager.getIntValue(KEY_READ_TIMEOUT, 50);
	private static int connectTimeout = configManager.getIntValue(KEY_CONNECT_TIMEOUT, 1000);
	private static int connections = configManager.getIntValue(KEY_CONNECTIONS, 4);
	private static int callbackQueueSize = configManager.getIntValue(KEY_CALLBACK_QUEUESIZE, 10000);
	private static int callbackCoreSize = configManager.getIntValue(KEY_CALLBACK_CORESIZE, 5);
	private static int callbackMaxSize = configManager.getIntValue(KEY_CALLBACK_MAXSIZE, 16);

	private static Logger logger = LoggerFactory.getLogger(DCacheClientImpl.class);

	private DCacheClientConfig config;

	DCacheKVClientAPI kvClient;

	DCacheClientAPI client;

	private String key;

	@Override
	public void initialize(CacheClientConfiguration config) {
		this.config = (DCacheClientConfig) config;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public void start() {
		initClient();
		try {
			ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new ConfigChangeHandler());
		} catch (Exception e) {
			logger.warn("", e);
		}
	}

	private void initClient() {
		if (this.config == null || this.config.getLocator() == null || this.config.getTranscoder() == null
				|| this.config.getModule() == null || this.config.getProxy() == null) {
			throw new StoreInitializeException("invalid dcache config");
		}
		System.setProperty("com.qq.nami.client.selectorPoolSize", "1");
		logger.info("dcache read timeout:" + readTimeout);
		ConnectionConf connectionConfig = new ConnectionConf();
		connectionConfig.setConnectTimeout(connectTimeout);
		connectionConfig.setSyncTimeout(readTimeout);
		connectionConfig.setConnections(connections);
		connectionConfig.setClientCorePoolSize(callbackCoreSize);
		connectionConfig.setClientMaxPoolSize(callbackMaxSize);
		connectionConfig.setClientQueueSize(callbackQueueSize);
		try {
			kvClient = ClientFactory.getKvClientAPI(this.config.getModule(), this.config.getProxy(),
					this.config.getLocator(), connectionConfig);

			client = ClientFactory.getClientAPI(this.config.getModule(), this.config.getProxy(),
					this.config.getLocator(), connectionConfig);
		} catch (Exception e) {
			throw new StoreInitializeException("error while initializing dcache", e);
		}
	}

	private class ConfigChangeHandler implements ConfigChangeListener {

		@Override
		public void onChange(String key, String value) {
			if (key.endsWith(KEY_READ_TIMEOUT)) {
				readTimeout = Integer.valueOf(value);
				initClient();
			} else if (key.endsWith(KEY_CONNECT_TIMEOUT)) {
				connectTimeout = Integer.valueOf(value);
				initClient();
			} else if (key.endsWith(KEY_CONNECTIONS)) {
				connections = Integer.valueOf(value);
				initClient();
			}
		}
	}

	@Override
	public void stop() {

	}

	public DCacheKVClientAPI getKVClient() {
		return kvClient;
	}

	public DCacheClientAPI getClient() {
		return client;
	}

	@Override
	public Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		key = CacheKeyUtils.reformKey(key);
		final CacheFuture<Boolean> future = new CacheFuture<Boolean>(key);
		try {
			ClientCallback<KVCacheResult> callback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						future.onSuccess(true);
					} else {
						future.onFailure(new StoreException("dcache operation failed, error code:" + result.getCode()));
					}
				}

				@Override
				public void onException(Throwable th) {
					future.onFailure(th);
				}

				@Override
				public void onTimeout() {
					future.onFailure(new TimeoutException("DCache timeout"));
				}

			};
			if (expiration <= 0) {
				getKVClient().asyncSet(key, value, callback);
			} else {
				getKVClient().asyncSet(key, value, (byte) 0, true, expiration, callback);
			}
			return future;
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	@Override
	public boolean set(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return set(key, value, expiration, 0, isHot, category);
	}

	@Override
	public boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		key = CacheKeyUtils.reformKey(key);
		try {
			KVCacheResult result = null;
			if (expiration <= 0) {
				result = getKVClient().set(key, value);
			} else {
				result = getKVClient().set(key, value, (byte) 0, expiration);
			}
			if (result != null) {
				if (result.getCode() == DCacheConst.ET_SUCC) {
					return true;
				}
			}
			throw new StoreException("dcache operation failed, error code:" + (result == null ? "" : result.getCode()));
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	@Override
	public boolean add(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
			TimeoutException {
		return add(key, value, expiration, 0, isHot, category);
	}

	@Override
	public boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException, TimeoutException {
		key = CacheKeyUtils.reformKey(key);
		try {
			KVCacheResult result = null;
			if (expiration <= 0) {
				result = getKVClient().add(key, value);
			} else {
				result = getKVClient().add(key, value, expiration);
			}
			if (result != null) {
				if (result.getCode() == DCacheConst.ET_SUCC) {
					return true;
				}
				if (result.getCode() == DCacheConst.ET_DATA_EXIST) {
					return false;
				}
				if (result.getCode() == DCacheConst.ET_DATA_VER_MISMATCH) {
					return false;
				}
			}
			throw new StoreException("dcache operation failed, error code:" + (result == null ? "" : result.getCode()));
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	@Override
	public Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException {
		key = CacheKeyUtils.reformKey(key);
		final CacheFuture<Boolean> future = new CacheFuture<Boolean>(key);
		try {
			ClientCallback<KVCacheResult> callback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						future.onSuccess(true);
					} else {
						future.onFailure(new StoreException("dcache operation failed, error code:" + result.getCode()));
					}
				}

				@Override
				public void onException(Throwable th) {
					future.onFailure(th);
				}

				@Override
				public void onTimeout() {
					future.onFailure(new TimeoutException("DCache timeout"));
				}

			};
			getKVClient().asyncSet(key, value, (byte) 1, true, expiration, callback);
			return future;
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	@Override
	public void replace(String key, Object value, int expiration, boolean isHot, String category) throws Exception {
		throw new UnsupportedOperationException("");
	}

	@Override
	public <T> T get(String key, Class dataType, String category) throws Exception {
		try {
			return get(key, Object.class, false, category, false);
		} catch (TimeoutException e) {
			return null;
		}
	}

	@Override
	public <T> T get(String key, Class dataType, boolean isHot, String category, boolean timeoutAware) throws Exception {
		key = CacheKeyUtils.reformKey(key);
		try {
			KVCacheResult result = getKVClient().get(key);
			if (result != null) {
				if (result.getCode() == DCacheConst.ET_SUCC) {
					return (T) result.getValue();
				} else if (result.getCode() == DCacheConst.ET_NO_DATA) {
					return null;
				}
			}
			throw new StoreException("dcache operation failed, error code:" + (result == null ? "" : result.getCode()));
		} catch (Exception e) {
			if (e instanceof StoreException) {
				throw e;
			} else {
				throw new StoreException(e);
			}
		}
	}

	@Override
	public <T> Map<String, T> getBulk(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			boolean timeoutAware) throws Exception {
		keys = CacheKeyUtils.reformKeys(keys);
		Map<String, T> result = null;
		List<Object> list = new ArrayList<Object>(keys.size());
		for (String k : keys) {
			list.add(k);
		}
		String category = null;
		if (categories != null && !categories.isEmpty()) {
			category = categories.values().iterator().next();
		}
		try {
			BatchKVCacheResult batchResult = getKVClient().batchGet(list);
			if (batchResult != null) {
				if (batchResult.getCode() == DCacheConst.ET_SUCC) {
					result = new HashMap<String, T>();
					Map<Object, CacheValue> resultMap = batchResult.getValueMap();
					Set<Entry<Object, CacheValue>> entrySet = resultMap.entrySet();
					for (Entry<Object, CacheValue> entry : entrySet) {
						CacheValue keyResult = entry.getValue();
						result.put((String) entry.getKey(), (T) keyResult.getValue());
					}
					return CacheKeyUtils.reformBackKeys(result);
				} else if (batchResult.getCode() == DCacheConst.ET_KEY_INVALID
						|| batchResult.getCode() == DCacheConst.ET_INPUT_PARAM_ERROR) {
					return null;
				}
			}
			throw new StoreException("dcache operation failed, error code:"
					+ (result == null ? "" : batchResult.getCode()));
		} catch (Exception e) {
			if (e instanceof StoreException) {
				throw e;
			} else {
				throw new StoreException(e);
			}
		}
	}

	@Override
	public boolean delete(String key, boolean isHot, String category) throws StoreException, TimeoutException {
		return this.delete(key, isHot, category, 0);
	}

	@Override
	public boolean delete(String key, boolean isHot, String category, long timeout) throws StoreException,
			TimeoutException {
		key = CacheKeyUtils.reformKey(key);
		try {
			KVCacheResult result = getKVClient().delete(key);
			if (result != null) {
				if (result.getCode() == DCacheConst.ET_SUCC) {
					return true;
				} else if (result.getCode() == DCacheConst.ET_KEY_INVALID
						|| result.getCode() == DCacheConst.ET_KEY_AREA_ERR
						|| result.getCode() == DCacheConst.ET_NO_DATA
						|| result.getCode() == DCacheConst.ET_KEY_TYPE_ERR) {
					return false;
				}
			}
			throw new StoreException("dcache operation failed, error code:" + (result == null ? "" : result.getCode()));
		} catch (Exception e) {
			if (e instanceof StoreException) {
				throw (StoreException) e;
			} else if (e instanceof TimeoutException) {
				throw (TimeoutException) e;
			} else {
				throw new StoreException(e);
			}
		}
	}

	@Override
	public Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws StoreException {
		key = CacheKeyUtils.reformKey(key);
		final CacheFuture<Boolean> future = new CacheFuture<Boolean>(key);
		try {
			ClientCallback<KVCacheResult> callback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						future.onSuccess(true);
					} else if (result.getCode() == DCacheConst.ET_KEY_INVALID
							|| result.getCode() == DCacheConst.ET_KEY_AREA_ERR
							|| result.getCode() == DCacheConst.ET_NO_DATA
							|| result.getCode() == DCacheConst.ET_KEY_TYPE_ERR) {
						future.onSuccess(false);
					} else {
						future.onFailure(new StoreException("dcache operation failed, error code:" + result.getCode()));
					}
				}

				@Override
				public void onException(Throwable th) {
					future.onFailure(th);
				}

				@Override
				public void onTimeout() {
					future.onFailure(new TimeoutException("DCache timeout"));
				}

			};
			getKVClient().asyncDelete(key, callback);
			return future;
		} catch (Exception e) {
			if (e instanceof StoreException) {
				throw (StoreException) e;
			} else {
				throw new StoreException(e);
			}
		}
	}

	@Override
	public long increment(String key, int amount, String category) throws StoreException, TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public long increment(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public long decrement(String key, int amount, String category) throws StoreException, TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public long decrement(String key, int amount, String category, long def) throws StoreException, TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public void clear() throws Exception {

	}

	@Override
	public boolean isDistributed() {
		return true;
	}

	@Override
	public <T> CASValue<T> gets(String key, String category) throws StoreException, TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public CASResponse cas(String key, long casId, Object value, String category) throws StoreException,
			TimeoutException {
		throw new UnsupportedOperationException("");
	}

	@Override
	public void asyncSet(String key, Object value, int expiration, boolean isHot, String category,
			final CacheCallback<Boolean> callback) {
		key = CacheKeyUtils.reformKey(key);
		try {
			ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						callback.onSuccess(true);
					} else {
						callback.onFailure("DCache result code:" + result.getCode(), null);
					}
				}

				@Override
				public void onException(Throwable th) {
					callback.onFailure("DCache error", th);
				}

				@Override
				public void onTimeout() {
					callback.onFailure("DCache timeout", new TimeoutException("DCache timeout"));
				}

			};
			if (expiration <= 0) {
				getKVClient().asyncSet(key, value, dcacheCallback);
			} else {
				getKVClient().asyncSet(key, value, (byte) 0, true, expiration, dcacheCallback);
			}
		} catch (Throwable e) {
			callback.onFailure("", e);
		}
	}

	@Override
	public void asyncAdd(String key, Object value, int expiration, boolean isHot, String category,
			final CacheCallback<Boolean> callback) {
		key = CacheKeyUtils.reformKey(key);
		try {
			ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						callback.onSuccess(true);
					} else {
						callback.onFailure("DCache result code:" + result.getCode(), null);
					}
				}

				@Override
				public void onException(Throwable th) {
					callback.onFailure("DCache error", th);
				}

				@Override
				public void onTimeout() {
					callback.onFailure("DCache timeout", new TimeoutException("DCache timeout"));
				}

			};
			getKVClient().asyncSet(key, value, (byte) 1, true, expiration, dcacheCallback);
		} catch (Exception e) {
			callback.onFailure("", e);
		}
	}

	@Override
	public <T> Future<T> asyncGet(String key, Class dataType, boolean isHot, String category) throws StoreException {
		key = CacheKeyUtils.reformKey(key);
		final CacheFuture<T> future = new CacheFuture<T>(key);
		try {
			ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						future.onSuccess((T) result.getValue());
					} else {
						future.onFailure(new StoreException("DCache result code:" + result.getCode()));
					}
				}

				@Override
				public void onException(Throwable th) {
					future.onFailure(th);
				}

				@Override
				public void onTimeout() {
					future.onFailure(new TimeoutException("DCache timeout"));
				}

			};
			getKVClient().asyncGet(key, dcacheCallback);
		} catch (Exception e) {
			if (e instanceof StoreException) {
				throw (StoreException) e;
			} else {
				throw new StoreException(e);
			}
		}
		return future;
	}

	@Override
	public <T> void asyncGet(String key, Class dataType, boolean isHot, String category, final CacheCallback<T> callback) {
		key = CacheKeyUtils.reformKey(key);
		try {
			ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

				@Override
				public void onCompleted(KVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						callback.onSuccess((T) result.getValue());
					} else {
						callback.onFailure("DCache result code:" + result.getCode(), null);
					}
				}

				@Override
				public void onException(Throwable th) {
					callback.onFailure("DCache error", th);
				}

				@Override
				public void onTimeout() {
					callback.onFailure("DCache timeout", new TimeoutException("DCache timeout"));
				}

			};
			getKVClient().asyncGet(key, dcacheCallback);
		} catch (Exception e) {
			callback.onFailure("", e);
		}
	}

	@Override
	public <T> void asyncBatchGet(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
			final CacheCallback<Map<String, T>> callback) {
		try {
			ClientCallback<BatchKVCacheResult> dcacheCallback = new ClientCallback<BatchKVCacheResult>() {

				@Override
				public void onCompleted(BatchKVCacheResult result) {
					if (result.getCode() == DCacheConst.ET_SUCC) {
						Map<Object, CacheValue> dcacheResults = result.getValueMap();
						Map<String, T> results = new HashMap<String, T>(dcacheResults.size());
						for (Object k : dcacheResults.keySet()) {
							results.put(k.toString(), (T) dcacheResults.get(k).getValue());
						}
						callback.onSuccess(results);
					} else {
						callback.onFailure("DCache result code:" + result.getCode(), null);
					}
				}

				@Override
				public void onException(Throwable th) {
					callback.onFailure("DCache error", th);
				}

				@Override
				public void onTimeout() {
					callback.onFailure("DCache timeout", new TimeoutException("DCache timeout"));
				}

			};
			List<Object> keyList = new ArrayList<Object>(keys.size());
			for (String key : keys) {
				keyList.add(key);
			}
			getKVClient().asyncBatchGet(keyList, dcacheCallback);
		} catch (Exception e) {
			callback.onFailure("", e);
		}
	}

    @Override
    public <T> void asyncBatchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category,
            final CacheCallback<Boolean> callback) {
        ClientCallback<BatchKVCacheResult> dcacheCallback = null;
        if(callback != null) {
            dcacheCallback = new ClientCallback<BatchKVCacheResult>() {

                @Override
                public void onCompleted(BatchKVCacheResult result) {
                    if (result.getCode() == DCacheConst.ET_SUCC) {
                        callback.onSuccess(true);
                    } else {
                        callback.onFailure("dcache batchset error code:" + result.getCode(), null);
                    }
                }

                @Override
                public void onException(Throwable th) {
                    callback.onFailure("dcache batchset error", th);
                }

                @Override
                public void onTimeout() {
                    callback.onFailure("dcache batchset timeout", new TimeoutException("dcache batchset timeout"));
                }

            };
        }
        
        try {
            Map<Object, InputValue> kvs = new HashMap<Object, InputValue>();
            for(int i=0; i<keys.size(); i++) {
                String key = keys.get(i);
                InputValue iv = getInputValue(values.get(i), expiration);
                kvs.put(key, iv);
            }
            getKVClient().asyncBatchSet(kvs, dcacheCallback);
        } catch (Exception e) {
            callback.onFailure("", e);
        }
    }
    
    @Override
    public <T> boolean batchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category)
            throws StoreException, TimeoutException {
        BatchKVCacheResult result = null;
        Map<Object, InputValue> kvs = new HashMap<Object, InputValue>();
        for(int i=0; i<keys.size(); i++) {
            String key = keys.get(i);
            InputValue iv = getInputValue(values.get(i), expiration);
            kvs.put(key, iv);
        }
        try {
            result = getKVClient().batchSet(kvs);
            if(result.getCode() == DCacheConst.ET_SUCC) {
                return true;
            }
            throw new StoreException("dcache batch set failed, error code: " + result.getCode());
        } catch(TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new StoreException(e);
        }
    }
    
    private InputValue getInputValue(Object value, int expiration) {
        if(expiration <= 0) {
            return new InputValue(value, (byte)0, 0);
        } else {
            return new InputValue(value, (byte)0, expiration);
        }
    }

}
