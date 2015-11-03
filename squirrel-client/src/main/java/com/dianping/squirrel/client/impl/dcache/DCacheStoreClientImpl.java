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

import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreFuture;
import com.dianping.squirrel.client.core.StoreTypeAware;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.client.impl.AbstractStoreClient;
import com.dianping.squirrel.client.impl.memcached.CASResponse;
import com.dianping.squirrel.client.impl.memcached.CASValue;
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

public class DCacheStoreClientImpl extends AbstractStoreClient implements DCacheStoreClient, Lifecycle, StoreTypeAware {

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

	private static Logger logger = LoggerFactory.getLogger(DCacheStoreClientImpl.class);

	private DCacheClientConfig config;

	DCacheKVClientAPI kvClient;

	DCacheClientAPI client;

	private String storeType;

	@Override
	public void initialize(StoreClientConfig config) {
		this.config = (DCacheClientConfig) config;
	}

	@Override
	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	@Override
	public String getStoreType() {
		return storeType;
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
    
    private InputValue getInputValue(Object value, int expiration) {
        if(expiration <= 0) {
            return new InputValue(value, (byte)0, 0);
        } else {
            return new InputValue(value, (byte)0, expiration);
        }
    }

    @Override
    protected <T> T doGet(CacheKeyType categoryConfig, String finalKey) throws Exception {
        KVCacheResult result = getKVClient().get(finalKey);
        if (result != null) {
            if (result.getCode() == DCacheConst.ET_SUCC) {
                return (T) result.getValue();
            } else if (result.getCode() == DCacheConst.ET_NO_DATA) {
                return null;
            }
        }
        throw new StoreException("dcache get failed, error code: " + (result == null ? "" : result.getCode()));
    }

    @Override
    protected Boolean doSet(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception {
        KVCacheResult result = null;
        int expiration = categoryConfig.getDurationSeconds();
        if (expiration <= 0) {
            result = getKVClient().set(finalKey, value);
        } else {
            result = getKVClient().set(finalKey, value, (byte) 0, expiration);
        }
        if (result != null) {
            if (result.getCode() == DCacheConst.ET_SUCC) {
                return true;
            }
        }
        throw new StoreException("dcache set failed, error code: " + (result == null ? "" : result.getCode()));
    }

    @Override
    protected Boolean doAdd(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception {
        KVCacheResult result = null;
        int expiration = categoryConfig.getDurationSeconds();
        if (expiration <= 0) {
            result = getKVClient().add(finalKey, value);
        } else {
            result = getKVClient().add(finalKey, value, expiration);
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
        throw new StoreException("dcache add failed, error code: " + (result == null ? "" : result.getCode()));
    }

    @Override
    protected Boolean doDelete(CacheKeyType categoryConfig, String finalKey) throws Exception {
        KVCacheResult result = getKVClient().delete(finalKey);
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
        throw new StoreException("dcache delete failed, error code: " + (result == null ? "" : result.getCode()));
    }

    @Override
    protected <T> Future<T> doAsyncGet(CacheKeyType categoryConfig, String finalKey) throws Exception {
        final StoreFuture<T> future = new StoreFuture<T>(finalKey);
        ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    future.onSuccess((T) result.getValue());
                } else {
                    future.onFailure(new StoreException("dcache async get failed, error code: " + result.getCode()));
                }
            }

            @Override
            public void onException(Throwable th) {
                future.onFailure(th);
            }

            @Override
            public void onTimeout() {
                future.onFailure(new TimeoutException("dcache async get timeout"));
            }
            
        };
        getKVClient().asyncGet(finalKey, dcacheCallback);
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
        ClientCallback<KVCacheResult> callback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    future.onSuccess(true);
                } else {
                    future.onFailure(new StoreException("dcache async set failed, error code: " + result.getCode()));
                }
            }

            @Override
            public void onException(Throwable th) {
                future.onFailure(th);
            }

            @Override
            public void onTimeout() {
                future.onFailure(new TimeoutException("dcache async set timeout"));
            }

        };
        int expiration = categoryConfig.getDurationSeconds();
        if (expiration <= 0) {
            getKVClient().asyncSet(finalKey, value, callback);
        } else {
            getKVClient().asyncSet(finalKey, value, (byte) 0, true, expiration, callback);
        }
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
        ClientCallback<KVCacheResult> callback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    future.onSuccess(true);
                } else if(result.getCode() == DCacheConst.ET_DATA_VER_MISMATCH) {
                    future.onSuccess(false);
                } else {
                    future.onFailure(new StoreException("dcache async add failed, error code: " + result.getCode()));
                }
            }

            @Override
            public void onException(Throwable th) {
                future.onFailure(th);
            }

            @Override
            public void onTimeout() {
                future.onFailure(new TimeoutException("dcache async add timeout"));
            }

        };
        getKVClient().asyncSet(finalKey, value, (byte) 1, true, categoryConfig.getDurationSeconds(), callback);
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncDelete(CacheKeyType categoryConfig, String finalKey) throws Exception {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
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
                    future.onFailure(new StoreException("dcache async delete failed, error code: " + result.getCode()));
                }
            }

            @Override
            public void onException(Throwable th) {
                future.onFailure(th);
            }

            @Override
            public void onTimeout() {
                future.onFailure(new TimeoutException("dcache async delete timeout"));
            }

        };
        getKVClient().asyncDelete(finalKey, callback);
        return future;
    }

    @Override
    protected <T> Void doAsyncGet(CacheKeyType categoryConfig, String finalKey, final StoreCallback<T> callback) throws Exception {
        ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    callback.onSuccess((T) result.getValue());
                } else {
                    callback.onFailure("dcache async get failed, error code: " + result.getCode(), null);
                }
            }

            @Override
            public void onException(Throwable th) {
                callback.onFailure("dcache async get failed", th);
            }

            @Override
            public void onTimeout() {
                callback.onFailure("dcache async get timeout", new TimeoutException("dcache async get timeout"));
            }

        };
        getKVClient().asyncGet(finalKey, dcacheCallback);
        return null;
    }

    @Override
    protected Void doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value, final StoreCallback<Boolean> callback) throws Exception {
        ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure("dcache async set failed, error code: " + result.getCode(), null);
                }
            }

            @Override
            public void onException(Throwable th) {
                callback.onFailure("dcache async set failed", th);
            }

            @Override
            public void onTimeout() {
                callback.onFailure("dcache async set timeout", new TimeoutException("dcache async set timeout"));
            }

        };
        int expiration = categoryConfig.getDurationSeconds();
        if (expiration <= 0) {
            getKVClient().asyncSet(finalKey, value, dcacheCallback);
        } else {
            getKVClient().asyncSet(finalKey, value, (byte) 0, true, expiration, dcacheCallback);
        }
        return null;
    }

    @Override
    protected Void doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value,
                              final StoreCallback<Boolean> callback) throws Exception {
        ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure("dcache async add failed, error code: " + result.getCode(), null);
                }
            }

            @Override
            public void onException(Throwable th) {
                callback.onFailure("dcache async add failed", th);
            }

            @Override
            public void onTimeout() {
                callback.onFailure("dcache async add timeout", new TimeoutException("dcache async add timeout"));
            }

        };
        getKVClient().asyncSet(finalKey, value, (byte) 1, true, categoryConfig.getDurationSeconds(), dcacheCallback);
        return null;
    }

    @Override
    protected Void doAsyncDelete(CacheKeyType categoryConfig, String finalKey, final StoreCallback<Boolean> callback) throws Exception {
        final ClientCallback<KVCacheResult> dcacheCallback = new ClientCallback<KVCacheResult>() {

            @Override
            public void onCompleted(KVCacheResult result) {
                if (result.getCode() == DCacheConst.ET_SUCC) {
                    callback.onSuccess(true);
                } else if (result.getCode() == DCacheConst.ET_KEY_INVALID
                        || result.getCode() == DCacheConst.ET_KEY_AREA_ERR
                        || result.getCode() == DCacheConst.ET_NO_DATA
                        || result.getCode() == DCacheConst.ET_KEY_TYPE_ERR) {
                    callback.onSuccess(false);
                } else {
                    callback.onFailure("dcache async delete failed, error code: " + result.getCode(), null);
                }
            }

            @Override
            public void onException(Throwable th) {
                callback.onFailure("dcache async delete failed", th);
            }

            @Override
            public void onTimeout() {
                callback.onFailure("dcache async delete timeout", new TimeoutException("dcache async delete timeout"));
            }

        };
        getKVClient().asyncDelete(finalKey, dcacheCallback);
        return null;
    }

    @Override
    protected Long doIncrease(CacheKeyType categoryConfig, String finalKey, int amount) {
        throw new UnsupportedOperationException("dcache does not support increase operation");
    }

    @Override
    protected Long doDecrease(CacheKeyType categoryConfig, String finalKey, int amount) {
        throw new UnsupportedOperationException("dcache does not support decrease operation");
    }

    @Override
    protected <T> Map<String, T> doMultiGet(CacheKeyType categoryConfig, List<String> keys) throws Exception {
        Map<String, T> result = null;
        List<Object> list = new ArrayList<Object>(keys);
        
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
                return result;
            } else if (batchResult.getCode() == DCacheConst.ET_KEY_INVALID
                || batchResult.getCode() == DCacheConst.ET_INPUT_PARAM_ERROR) {
                return null;
            }
        }
        throw new StoreException("dcache operation failed, error code: "
            + (result == null ? "" : batchResult.getCode()));
    }

    @Override
    protected <T> Void doAsyncMultiGet(CacheKeyType categoryConfig, List<String> finalKeyList,
                                       final StoreCallback<Map<String, T>> callback) throws Exception {
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
                    callback.onFailure("dcache async multi get failed, result code: " + result.getCode(), null);
                }
            }

            @Override
            public void onException(Throwable th) {
                callback.onFailure("dcache async multi get failed", th);
            }

            @Override
            public void onTimeout() {
                callback.onFailure("dcache async multi get timeout", new TimeoutException("dcache async multi get timeout"));
            }

        };
        
        List<Object> keyList = new ArrayList<Object>(finalKeyList);
        getKVClient().asyncBatchGet(keyList, dcacheCallback);
        return null;
    }

    @Override
    protected <T> Boolean doMultiSet(CacheKeyType categoryConfig, List<String> keys, List<T> values) throws Exception {
        BatchKVCacheResult result = null;
        Map<Object, InputValue> kvs = new HashMap<Object, InputValue>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            InputValue iv = getInputValue(values.get(i), categoryConfig.getDurationSeconds());
            kvs.put(key, iv);
        }
        
        result = getKVClient().batchSet(kvs);
        if (result.getCode() == DCacheConst.ET_SUCC) {
            return true;
        }
        throw new StoreException("dcache multi set failed, error code: " + result.getCode());
    }

    @Override
    public <T> Void doAsyncMultiSet(CacheKeyType categoryConfig, List<String> keys, List<T> values,
                                    final StoreCallback<Boolean> callback) throws Exception {
        ClientCallback<BatchKVCacheResult> dcacheCallback = null;
        if (callback != null) {
            dcacheCallback = new ClientCallback<BatchKVCacheResult>() {

                @Override
                public void onCompleted(BatchKVCacheResult result) {
                    if (result.getCode() == DCacheConst.ET_SUCC) {
                        callback.onSuccess(true);
                    } else {
                        callback.onFailure("dcache async multi set failed, error code: " + result.getCode(), null);
                    }
                }

                @Override
                public void onException(Throwable th) {
                    callback.onFailure("dcache async multi set failed", th);
                }

                @Override
                public void onTimeout() {
                    callback.onFailure("dcache async multi set timeout", new TimeoutException("dcache async multi set timeout"));
                }

            };
        }

        Map<Object, InputValue> kvs = new HashMap<Object, InputValue>();
        int expiration = categoryConfig.getDurationSeconds();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            InputValue iv = getInputValue(values.get(i), expiration);
            kvs.put(key, iv);
        }
        getKVClient().asyncBatchSet(kvs, dcacheCallback);
        return null;
    }

}