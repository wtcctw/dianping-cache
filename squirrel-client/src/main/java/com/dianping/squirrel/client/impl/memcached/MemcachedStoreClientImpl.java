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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetCompletionListener;
import net.spy.memcached.internal.BulkGetFuture;
import net.spy.memcached.internal.CheckedOperationTimeoutException;
import net.spy.memcached.internal.GetCompletionListener;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationCompletionListener;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreTypeAware;
import com.dianping.squirrel.client.impl.AbstractStoreClient;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreTimeoutException;
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
public class MemcachedStoreClientImpl extends AbstractStoreClient implements MemcachedStoreClient, Lifecycle,
        StoreTypeAware {

    private static Logger logger = LoggerFactory.getLogger(MemcachedStoreClientImpl.class);

    /**
     * in milliseconds
     */
    private static final long DEFAULT_GET_TIMEOUT = 50;

    private static final long DEFAULT_MGET_TIMEOUT = 80;

    private static final long DEFAULT_ADD_TIMEOUT = 50;

    private static final long DEFAULT_SET_TIMEOUT = 50;

    private static final String KEY_GET_TIMEOUT = "avatar-cache.memcached.get.timeout";

    private static final String KEY_MGET_TIMEOUT = "avatar-cache.memcached.mget.timeout";

    private static final String KEY_ADD_TIMEOUT = "avatar-cache.memcached.add.timeout";

    private static final String KEY_HOTKEY_LOCKTIME = "avatar-cache.memcached.hotkey.locktime";

    private static final String MSG_SUCCESS = Message.SUCCESS;

    private static final String MSG_TIMEOUT = "-1";

    private static final String MSG_EXCEPTION = "-2";

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static long timeoutMGet = configManager.getLongValue(KEY_MGET_TIMEOUT, DEFAULT_MGET_TIMEOUT);

    private static long timeoutGet = configManager.getLongValue(KEY_GET_TIMEOUT, DEFAULT_GET_TIMEOUT);

    private static long timeoutAdd = configManager.getLongValue(KEY_ADD_TIMEOUT, DEFAULT_ADD_TIMEOUT);

    private static long timeoutSet = configManager.getLongValue(KEY_ADD_TIMEOUT, DEFAULT_SET_TIMEOUT);

    private static int hotkeyLockTime = configManager.getIntValue(KEY_HOTKEY_LOCKTIME, 30);

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
     * Spymemcached client configuration
     */
    private MemcachedClientConfig config;

    private volatile MemcachedClientManager clientManager;

    static {
        try {
            configManager.registerConfigChangeListener(new ConfigChangeHandler());
            String timeoutConfig = configManager.getStringValue(KEY_GET_TIMEOUT_LIST, "50");
            RetryLoop.initTimeoutConfig(timeoutConfig);
        } catch (Exception e) {
        }
    }

    @Override
    public void initialize(StoreClientConfig config) {
        this.config = (MemcachedClientConfig) config;
        NodeMonitor.getInstance().clear(storeType);
    }

    @Override
    public void start() {
        clientManager = new MemcachedClientManager(storeType, config);
        clientManager.start();
        try {
            NodeMonitor.getInstance().addNodes(storeType, clientManager.getMemcachedClients());
        } catch (Exception e) {
            logger.warn("", e);
        }
    }

    @Override
    public void stop() {
        if (clientManager != null) {
            clientManager.stop();
        }
    }

    @Override
    public void configChanged(StoreClientConfig config) {
        logger.info("memcached store client config changed: " + config);
        this.config = (MemcachedClientConfig) config;
        MemcachedClientManager oldClientManager = this.clientManager;
        MemcachedClientManager newClientManager = new MemcachedClientManager(storeType, this.config);
        newClientManager.start();
        this.clientManager = newClientManager;
        oldClientManager.stop();
    }

    public MemcachedClient getReadClient() {
        return clientManager.getReadClient();
    }

    public MemcachedClient getWriteClient() {
        return clientManager.getWriteClient();
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

    @Override
    public <T> CASValue<T> gets(StoreKey key) throws StoreException {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = super.configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());

        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doGets(categoryConfig, finalKey);
            }

        }, categoryConfig, finalKey, "gets");
    }

    @Override
    public CASResponse cas(StoreKey key, final long casId, final Object value) throws StoreException {
        checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        final CacheKeyType categoryConfig = super.configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());

        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doCas(categoryConfig, finalKey, casId, value);
            }

        }, categoryConfig, finalKey, "cas");
    }

    public <T> CASValue<T> doGets(CacheKeyType categoryConfig, String key) throws StoreException {
        String reformedKey = CacheKeyUtils.reformKey(key);
        MemcachedClient client = getReadClient();
        try {
            net.spy.memcached.CASValue<T> v = (net.spy.memcached.CASValue<T>) client.gets(reformedKey);
            CASValue<T> casValue = null;
            if (v != null) {
                casValue = new CASValue<T>(v.getCas(), v.getValue());
            }
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, categoryConfig.getCategory());
            return casValue;
        } catch (OperationTimeoutException e) {
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, categoryConfig.getCategory());
            throw new StoreTimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, categoryConfig.getCategory());
            throw new StoreException(e);
        }
    }

    public CASResponse doCas(CacheKeyType categoryConfig, String key, long casId, Object value) throws StoreException {
        String reformedKey = CacheKeyUtils.reformKey(key);
        MemcachedClient client = getWriteClient();
        try {
            net.spy.memcached.CASResponse r = client.cas(reformedKey, casId, value);
            CASResponse casResponse = null;
            if (r != null) {
                casResponse = CASResponse.valueOf(r.name());
            }
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_SUCCESS, categoryConfig.getCategory());
            return casResponse;
        } catch (OperationTimeoutException e) {
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_TIMEOUT, categoryConfig.getCategory());
            throw new StoreTimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, reformedKey, MSG_EXCEPTION, categoryConfig.getCategory());
            throw new StoreException(e);
        }
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
            throw new StoreTimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, category);
            throw new StoreException(e);
        }
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
                Cat.logEvent("Store." + this.getStoreType(), category + ":setBackupFail", "-1", "key=" + key
                        + "&error=" + e.getMessage());
            }
        }
    }

    public void asyncRemoveBackupKey(String key, boolean isHot, String category) throws StoreException {
        if (isHot && removeBackup) {
            try {
                getWriteClient().delete(CacheKeyUtils.reformKey(key, true));
            } catch (RuntimeException e) {
                Cat.logEvent("Store." + this.getStoreType(), category + ":removeBackupFail", "-1", "key=" + key
                        + "&error=" + e.getMessage());
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
                    Cat.logEvent("Store." + this.getStoreType(), category + ":getLast", "0", lastVersionCacheKey);
                } else {
                    Cat.logEvent("Store." + this.getStoreType(), category + ":getLastMissed", "-1", lastVersionCacheKey);
                }
            } catch (TimeoutException e) {
                Cat.logEvent("Store." + this.getStoreType(), category + ":getLastTimeout", "-1", lastVersionCacheKey);
                logger.error("memcached get last key {} timeout", lastVersionCacheKey);
                throw e;
            }
        } else {
            Cat.logEvent("Store." + this.getStoreType(), category + ":lockAfterClear", "0", key);
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
                    Cat.logEvent("Store." + this.getStoreType(), category + ":getHot", "0", hotKey);
                } else {
                    Cat.logEvent("Store." + this.getStoreType(), category + ":getHotMissed", "-1", hotKey);
                }
            } catch (TimeoutException e) {
                Cat.logEvent("Store." + this.getStoreType(), category + ":getHotTimeout", "-1", hotKey);
                logger.error("memcached get hot key {} timeout", hotKey);
                throw e;
            }
        } else {
            Cat.logEvent("Store." + this.getStoreType(), category + ":lockAfterExp", "0", key);
            logger.info("memcached locked {} after expiration", lockKey);
            result = null;
        }
        return result;
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
    protected <T> T doGet(CacheKeyType categoryConfig, String key) throws Exception {
        boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
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
        if (timeoutException != null && result == null) {
            throw timeoutException;
        }
        return (T) result;
    }

    @Override
    protected Boolean doSet(CacheKeyType categoryConfig, String key, Object value) throws Exception {
        boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
        int expiration = categoryConfig.getDurationSeconds();
        String k = getCacheKey(key, isHot);
        Object v = getCacheValue(value, expiration, isHot);
        MemcachedClient client = getWriteClient();
        try {
            OperationFuture<Boolean> future = client.set(k, expiration, v);
            if (future != null) {
                boolean success = future.get(timeoutSet, TimeUnit.MILLISECONDS);
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
    protected Boolean doAdd(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception {
        boolean isHot = categoryConfig.isHot();
        int expiration = categoryConfig.getDurationSeconds();
        String category = categoryConfig.getCategory();

        String k = getCacheKey(finalKey, isHot);
        Object v = getCacheValue(value, expiration, isHot);
        MemcachedClient client = getWriteClient();
        try {
            OperationFuture<Boolean> future = client.add(k, expiration, v);
            if (future != null) {
                boolean result = future.get(timeoutAdd, TimeUnit.MILLISECONDS);
                if (result) {
                    asyncSetBackupKey(finalKey, value, expiration, isHot, category);
                }
                NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
                return result;
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
    protected Boolean doDelete(CacheKeyType categoryConfig, String finalKey) throws Exception {
        boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
        MemcachedClient client = getWriteClient();
        try {
            String reformedKey = getCacheKey(finalKey, isHot);
            Future<Boolean> future = client.delete(reformedKey);
            if (future != null) {
                boolean result = future.get(timeoutSet, TimeUnit.MILLISECONDS);
                if (result) {
                    asyncRemoveBackupKey(finalKey, isHot, category);
                }
                NodeMonitor.getInstance().logNode(client, finalKey, MSG_SUCCESS, category);
                return result;
            }
            return false;
        } catch (CheckedOperationTimeoutException e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_TIMEOUT, category);
            throw new TimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_EXCEPTION, category);
            throw new StoreException(e);
        }
    }

    @Override
    protected <T> GetFuture<T> doAsyncGet(CacheKeyType categoryConfig, String key) throws Exception {
        String finalKey = CacheKeyUtils.nextCacheKey(key, categoryConfig.isHot(), hotKeyHitRange);
        MemcachedClient client = getReadClient();
        try {
            GetFuture<T> future = (GetFuture<T>) client.asyncGet(finalKey);
            return future;
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, categoryConfig.getCategory());
            throw new StoreException(e);
        }
    }

    @Override
    protected OperationFuture<Boolean> doAsyncSet(CacheKeyType categoryConfig, String key, Object value)
            throws Exception {
        boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
        int expiration = categoryConfig.getDurationSeconds();
        String k = getCacheKey(key, isHot);
        Object v = getCacheValue(value, expiration, isHot);
        MemcachedClient client = getWriteClient();
        try {
            OperationFuture<Boolean> future = client.set(k, expiration, v);
            asyncSetBackupKey(k, value, expiration, isHot, category);
            NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
            return future;
        } catch (RuntimeException e) {
            NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
            throw new StoreException(e);
        }
    }

    @Override
    protected OperationFuture<Boolean> doAsyncAdd(CacheKeyType categoryConfig, String key, Object value)
            throws Exception {
        boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
        int expiration = categoryConfig.getDurationSeconds();
        String k = getCacheKey(key, isHot);
        Object v = getCacheValue(value, expiration, isHot);
        MemcachedClient client = getWriteClient();
        try {
            OperationFuture<Boolean> future = client.add(k, expiration, v);
            NodeMonitor.getInstance().logNode(client, k, MSG_SUCCESS, category);
            return future;
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, k, MSG_EXCEPTION, category);
            throw new StoreException(e);
        }
    }

    @Override
    protected OperationFuture<Boolean> doAsyncDelete(CacheKeyType categoryConfig, String key) throws Exception {
        String reformedKey = getCacheKey(key, categoryConfig.isHot());
        OperationFuture<Boolean> future = getWriteClient().delete(reformedKey);
        asyncRemoveBackupKey(key, categoryConfig.isHot(), categoryConfig.getCategory());
        return future;
    }

    @Override
    protected <T> Void doAsyncGet(final CacheKeyType categoryConfig, final String key, final StoreCallback<T> callback)
            throws Exception {
        GetFuture<T> future = doAsyncGet(categoryConfig, key);
        final MemcachedClient client = getReadClient();
        future.addListener(new GetCompletionListener() {

            @Override
            public void onComplete(GetFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if (status.isSuccess() || status.getStatusCode() == StatusCode.ERR_NOT_FOUND) {
                    T result = (T) future.get();
                    NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, categoryConfig.getCategory());
                    callback.onSuccess(result);
                } else {
                    if (status.getStatusCode() == StatusCode.TIMEDOUT) {
                        NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, categoryConfig.getCategory());
                        callback.onFailure(new StoreTimeoutException("memcached async get key " + key + " timeout"));
                    } else {
                        NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, categoryConfig.getCategory());
                        callback.onFailure(new StoreException("memcached async get key " + key + " failed, error: "
                                + status.getMessage()));
                    }
                }
            }

        });
        return null;
    }

    @Override
    protected Void doAsyncSet(final CacheKeyType categoryConfig, final String key, Object value,
            final StoreCallback<Boolean> callback) throws Exception {
        OperationFuture<Boolean> future = doAsyncSet(categoryConfig, key, value);
        final MemcachedClient client = getReadClient();
        future.addListener(new OperationCompletionListener() {

            @Override
            public void onComplete(OperationFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if (status.isSuccess()) {
                    Boolean result = (Boolean) future.get();
                    NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, categoryConfig.getCategory());
                    callback.onSuccess(result);
                } else {
                    if (status.getStatusCode() == StatusCode.TIMEDOUT) {
                        NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, categoryConfig.getCategory());
                        callback.onFailure(new StoreTimeoutException("memcached async set key " + key + " timeout"));
                    } else {
                        NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, categoryConfig.getCategory());
                        callback.onFailure(new StoreException("memcached async set key " + key + " failed, error: "
                                + status.getMessage()));
                    }
                }
            }

        });
        return null;
    }

    @Override
    protected Void doAsyncAdd(final CacheKeyType categoryConfig, final String key, Object value,
            final StoreCallback<Boolean> callback) throws Exception {
        OperationFuture<Boolean> future = doAsyncAdd(categoryConfig, key, value);
        final MemcachedClient client = getReadClient();
        future.addListener(new OperationCompletionListener() {

            @Override
            public void onComplete(OperationFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if (status.isSuccess() || status.getStatusCode() == StatusCode.ERR_EXISTS) {
                    Boolean result = (Boolean) future.get();
                    NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, categoryConfig.getCategory());
                    callback.onSuccess(result);
                } else {
                    if (status.getStatusCode() == StatusCode.TIMEDOUT) {
                        NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, categoryConfig.getCategory());
                        callback.onFailure(new StoreTimeoutException("memcached async add key " + key + " timeout"));
                    } else {
                        NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, categoryConfig.getCategory());
                        callback.onFailure(new StoreException("memcached async add key " + key + " failed, error: "
                                + status.getMessage()));
                    }
                }
            }

        });
        return null;
    }

    @Override
    protected Void doAsyncDelete(final CacheKeyType categoryConfig, final String key,
            final StoreCallback<Boolean> callback) throws Exception {
        OperationFuture<Boolean> future = doAsyncDelete(categoryConfig, key);
        final MemcachedClient client = getReadClient();
        future.addListener(new OperationCompletionListener() {

            @Override
            public void onComplete(OperationFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if (status.isSuccess() || status.getStatusCode() == StatusCode.ERR_NOT_FOUND) {
                    Boolean result = (Boolean) future.get();
                    NodeMonitor.getInstance().logNode(client, key, MSG_SUCCESS, categoryConfig.getCategory());
                    callback.onSuccess(result);
                } else {
                    if (status.getStatusCode() == StatusCode.TIMEDOUT) {
                        NodeMonitor.getInstance().logNode(client, key, MSG_TIMEOUT, categoryConfig.getCategory());
                        callback.onFailure(new StoreTimeoutException("memcached async delete key " + key + " timeout"));
                    } else {
                        NodeMonitor.getInstance().logNode(client, key, MSG_EXCEPTION, categoryConfig.getCategory());
                        callback.onFailure(new StoreException("memcached async delete key " + key + " failed, error: "
                                + status.getMessage()));
                    }
                }
            }

        });
        return null;
    }

    @Override
    protected Long doIncrease(CacheKeyType categoryConfig, String finalKey, int amount) throws Exception {
        MemcachedClient client = getWriteClient();
        try {
            long value = client.incr(finalKey, amount, (long) amount);
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_SUCCESS, categoryConfig.getCategory());
            return value;
        } catch (OperationTimeoutException e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_TIMEOUT, categoryConfig.getCategory());
            throw new StoreTimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_EXCEPTION, categoryConfig.getCategory());
            throw new StoreException(e);
        }
    }

    @Override
    protected Long doDecrease(CacheKeyType categoryConfig, String finalKey, int amount) throws Exception {
        MemcachedClient client = getWriteClient();
        try {
            long value = client.decr(finalKey, amount, (long) (0 - amount));
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_SUCCESS, categoryConfig.getCategory());
            return value;
        } catch (OperationTimeoutException e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_TIMEOUT, categoryConfig.getCategory());
            throw new StoreTimeoutException(e.getMessage());
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, finalKey, MSG_EXCEPTION, categoryConfig.getCategory());
            throw new StoreException(e);
        }
    }

    @Override
    protected <T> Map<String, T> doMultiGet(CacheKeyType categoryConfig, List<String> keys) throws Exception {
        MemcachedClient client = getReadClient();
        try {
            // use timeout to eliminate memcached servers' crash
            Map<String, T> result = (Map<String, T>) doGetBulk(client, keys, timeoutMGet);
            NodeMonitor.getInstance().logNode(client, keys, MSG_SUCCESS, "");
            return result;
        } catch (TimeoutException e) {
            NodeMonitor.getInstance().logNode(client, keys, MSG_TIMEOUT, "");
            throw new StoreTimeoutException(e);
        } catch (Exception e) {
            NodeMonitor.getInstance().logNode(client, keys, MSG_EXCEPTION, "");
            throw new StoreException(e);
        }
    }

    @Override
    protected <T> Void doAsyncMultiGet(CacheKeyType categoryConfig, final List<String> keys,
            final StoreCallback<Map<String, T>> callback) throws Exception {
        MemcachedClient client = getReadClient();

        BulkFuture<Map<String, Object>> future = client.asyncGetBulk(keys);
        future.addListener(new BulkGetCompletionListener() {

            @Override
            public void onComplete(BulkGetFuture<?> future) throws Exception {
                OperationStatus status = future.getStatus();
                if (status.isSuccess() || status.getStatusCode() == StatusCode.ERR_NOT_FOUND) {
                    Map<String, T> result = (Map<String, T>) future.get();
                    callback.onSuccess(result);
                } else if (status.getStatusCode() == StatusCode.TIMEDOUT) {
                    callback.onFailure(new StoreTimeoutException("memcached async multi get timeout"));
                } else {
                    callback.onFailure(new StoreException("memcached async multi get failed, error: "
                            + status.getMessage()));
                }
            }

        });
        return null;
    }

    @Override
    protected <T> Boolean doMultiSet(CacheKeyType categoryConfig, List<String> finalKeyList, List<T> values)
            throws Exception {
        throw new UnsupportedOperationException("memcached does not support multi set");
    }

    @Override
    public <T> Void doAsyncMultiSet(CacheKeyType categoryConfig, List<String> keys, List<T> values,
            StoreCallback<Boolean> callback) throws Exception {
        throw new UnsupportedOperationException("memcached does not support async multi set");
    }

}
