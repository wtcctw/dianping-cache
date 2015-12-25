package com.dianping.squirrel.client.impl.redis;

import static com.google.common.base.Preconditions.*;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCallback;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisConnectionException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.StoreCategoryConfig;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreFuture;
import com.dianping.squirrel.client.core.Transcoder;
import com.dianping.squirrel.client.impl.AbstractStoreClient;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreTimeoutException;

public class RedisStoreClientImpl extends AbstractStoreClient implements RedisStoreClient {

    private static Logger logger = LoggerFactory.getLogger(RedisStoreClientImpl.class);

    private static final String OK = "OK";

    private Transcoder<String> transcoder;
    
    private RedisClientManager clientManager = RedisClientManager.getInstance();
    
    private String eventType = "Squirrel.redis.server";
    
    private AtomicInteger timeoutExp = new AtomicInteger(0);
    private AtomicInteger connectExp = new AtomicInteger(0);
    private AtomicInteger noconnExp = new AtomicInteger(0);

    @Override
    public void setStoreType(String storeType) {
        super.setStoreType(storeType);
        eventType = "Squirrel." + storeType + ".server";
    }
    
    @Override
    public void configure(StoreClientConfig config) {
        clientManager.setClientConfig((RedisClientConfig) config);
    }

    @Override
    public void configChanged(StoreClientConfig config) {
        logger.info("redis store client config changed: " + config);
        clientManager.setClientConfig((RedisClientConfig) config);
    }
    
    @Override
    public void start() {
        transcoder = new RedisStringTranscoder(storeType);
    }

    @Override
    public void stop() {
        clientManager.closeClient();
    }

    protected <T> T executeWithMonitor(Command command, StoreCategoryConfig categoryConfig, String finalKey, String action) {
        String storeType = categoryConfig.getCacheType();
        String category = categoryConfig.getCategory();
        
        Transaction t = null;
        if (needMonitor(storeType)) {
            t = Cat.getProducer().newTransaction("Squirrel." + storeType, category + ":" + action);
            t.addData("finalKey", finalKey);
            t.setStatus(Message.SUCCESS);
        }
        StatusHolder.flowIn(storeType, category, action);
        long begin = System.nanoTime();
        int second = (int) (begin / 1000000000 % 60) + 1;
        try {
            Cat.getProducer().logEvent("Squirrel." + storeType + ".qps", "S"+second);
            Object result = command.execute();
            return (T) result;
        } catch (JedisConnectionException e) {
            StoreException se = null;
            if(e.getCause() instanceof SocketTimeoutException) {
                Cat.getProducer().logEvent("Squirrel." + storeType, category + ":timeout");
                if(e.getHost() != null) {
                    Cat.getProducer().logEvent(eventType, e.getHost() + ":" + e.getPort() + ":timeout");
                }
                se = new StoreTimeoutException(e.getCause());
                if(timeoutExp.incrementAndGet()%10 == 0) {
                    logger.error(finalKey, e);
                    Cat.getProducer().logError(se);
                }
            } else if(e.getCause() instanceof ConnectException) {
                Cat.getProducer().logEvent("Squirrel." + storeType, category + ":connect");
                if(e.getHost() != null) {
                    Cat.getProducer().logEvent(eventType, e.getHost() + ":" + e.getPort() + ":connect");
                }
                se = new StoreException(e.getCause());
                if(connectExp.incrementAndGet()%10 == 0) {
                    logger.error(finalKey, e);
                    Cat.getProducer().logError(se);
                }
            } else if(e.getCause() instanceof NoSuchElementException) {
                Cat.getProducer().logEvent("Squirrel." + storeType, category + ":noconn");
                if(e.getHost() != null) {
                    Cat.getProducer().logEvent(eventType, e.getHost() + ":" + e.getPort() + ":noconn");
                }
                se = new StoreException(e.getCause());
                if(noconnExp.incrementAndGet()%10 == 0) {
                    logger.error(finalKey, e);
                    Cat.getProducer().logError(se);
                }
            } else {
                se = new StoreException(e);
                logger.error(finalKey, e);
                Cat.getProducer().logError(se);
            }
            if (t != null) {
                t.setStatus(se);
            }
            throw se;
        } catch (Throwable e) {
            logger.error(finalKey, e);
            StoreException se = new StoreException(e);
            Cat.getProducer().logError(se);
            if (t != null) {
                t.setStatus(se);
            }
            throw se;
        } finally {
            StatusHolder.flowOut(storeType, category, action);
            if (t != null) {
                t.complete();
            }
        }
    }
    
    @Override
    protected <T> T doGet(StoreCategoryConfig categoryConfig, String finalKey) {
        String value = clientManager.getClient().get(finalKey);
        if(value != null) {
            T object = transcoder.decode(value);
            return object;
        } else {
            return null;
        }
    }

    @Override
    protected Boolean doSet(StoreCategoryConfig categoryConfig, String finalKey, Object value) {
        String result = null;
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0)
            result = clientManager.getClient().setex(finalKey, categoryConfig.getDurationSeconds(), str);
        else
            result = clientManager.getClient().set(finalKey, str);
        return OK.equals(result);
    }

    @Override
    protected Boolean doAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value) {
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            String result = clientManager.getClient().set(finalKey, str, "NX", "EX", categoryConfig.getDurationSeconds());
            return OK.equals(result);
        } else {
            long result = clientManager.getClient().setnx(finalKey, str);
            return 1 == result;
        }
    }

    @Override
    protected Boolean doDelete(StoreCategoryConfig categoryConfig, String finalKey) {
         long result = clientManager.getClient().del(finalKey);
         return 1 == result;
    }

    @Override
    protected Long doIncrease(StoreCategoryConfig categoryConfig, String finalKey, int amount) {
        long result = clientManager.getClient().incrBy(finalKey, amount);
        return result;
    }

    @Override
    protected Long doDecrease(StoreCategoryConfig categoryConfig, String finalKey, int amount) {
        long result = clientManager.getClient().decrBy(finalKey, amount);
        return result;
    }

    @Override
    protected <T> Future<T> doAsyncGet(StoreCategoryConfig categoryConfig, String finalKey) {
        final StoreFuture<T> future = new StoreFuture<T>(finalKey);
        clientManager.getClient().asyncGet(finalKey, new JedisCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if(result != null) {
                    T object = transcoder.decode(result);
                    future.onSuccess(object);
                } else {
                    future.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                future.onFailure(e);
            }
            
        });
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncSet(StoreCategoryConfig categoryConfig, String finalKey, Object value) {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
        String string = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            clientManager.getClient().asyncSetex(finalKey, categoryConfig.getDurationSeconds(), string, new JedisCallback<String>() {
    
                @Override
                public void onSuccess(String result) {
                    future.onSuccess(OK.equals(result));
                }
    
                @Override
                public void onFailure(Throwable e) {
                    future.onFailure(e);
                }
                
            });
        } else {
            clientManager.getClient().asyncSet(finalKey, string, new JedisCallback<String>() {
                
                @Override
                public void onSuccess(String result) {
                    future.onSuccess(OK.equals(result));
                }
    
                @Override
                public void onFailure(Throwable e) {
                    future.onFailure(e);
                }
                
            });
        }
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value) {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            clientManager.getClient().asyncSet(finalKey, str, "NX", "EX", categoryConfig.getDurationSeconds(), new JedisCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    future.onSuccess(OK.equals(result));
                }

                @Override
                public void onFailure(Throwable ex) {
                    future.onFailure(ex);
                }
                
            });
        } else {
            clientManager.getClient().asyncSetnx(finalKey, str, new JedisCallback<Long>() {

                @Override
                public void onSuccess(Long result) {
                    future.onSuccess(1 == result);
                }

                @Override
                public void onFailure(Throwable ex) {
                    future.onFailure(ex);
                }
                
            });
        }
        return future;
    }

    @Override
    protected Future<Boolean> doAsyncDelete(StoreCategoryConfig categoryConfig, String finalKey) {
        final StoreFuture<Boolean> future = new StoreFuture<Boolean>(finalKey);
        clientManager.getClient().asyncDel(finalKey, new JedisCallback<Long>() {

            @Override
            public void onSuccess(Long result) {
                future.onSuccess(1 == result);
            }

            @Override
            public void onFailure(Throwable ex) {
                future.onFailure(ex);
            }
            
        });
        return future;
    }

    @Override
    protected <T> Void doAsyncGet(StoreCategoryConfig categoryConfig, String finalKey, final StoreCallback<T> callback) {
        clientManager.getClient().asyncGet(finalKey, new JedisCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if(result != null) {
                    T object = transcoder.decode(result);
                    callback.onSuccess(object);
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                callback.onFailure(e);
            }
            
        });
        return null;
    }

    @Override
    protected Void doAsyncSet(StoreCategoryConfig categoryConfig, String finalKey, Object value, final StoreCallback<Boolean> callback) {
        String string = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            clientManager.getClient().asyncSetex(finalKey, categoryConfig.getDurationSeconds(), string, new JedisCallback<String>() {
    
                @Override
                public void onSuccess(String result) {
                    callback.onSuccess(OK.equals(result));
                }
    
                @Override
                public void onFailure(Throwable e) {
                    callback.onFailure(e);
                }
                
            });
        } else {
            clientManager.getClient().asyncSet(finalKey, string, new JedisCallback<String>() {
                
                @Override
                public void onSuccess(String result) {
                    callback.onSuccess(OK.equals(result));
                }
    
                @Override
                public void onFailure(Throwable e) {
                    callback.onFailure(e);
                }
                
            });
        }
        return null;
    }

    @Override
    protected Void doAsyncAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value,
                              final StoreCallback<Boolean> callback) {
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            clientManager.getClient().asyncSet(finalKey, str, "NX", "EX", categoryConfig.getDurationSeconds(), new JedisCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    callback.onSuccess(OK.equals(result));
                }

                @Override
                public void onFailure(Throwable ex) {
                    callback.onFailure(ex);
                }
                
            });
        } else {
            clientManager.getClient().asyncSetnx(finalKey, str, new JedisCallback<Long>() {

                @Override
                public void onSuccess(Long result) {
                    callback.onSuccess(1 == result);
                }

                @Override
                public void onFailure(Throwable ex) {
                    callback.onFailure(ex);
                }
                
            });
        }
        return null;
    }

    @Override
    protected Void doAsyncDelete(StoreCategoryConfig categoryConfig, String finalKey, final StoreCallback<Boolean> callback) {
        clientManager.getClient().asyncDel(finalKey, new JedisCallback<Long>() {

            @Override
            public void onSuccess(Long result) {
                callback.onSuccess(1 == result);
            }

            @Override
            public void onFailure(Throwable ex) {
                callback.onFailure(ex);
            }
            
        });
        return null;
    }
    

    @Override
    protected <T> Map<String, T> doMultiGet(StoreCategoryConfig categoryConfig, List<String> finalKeyList) throws Exception {
        Map<String, String> map = clientManager.getClient().mget(finalKeyList.toArray(new String[0]));
        if(map != null) {
            Map<String, T> resultMap = new HashMap<String, T>(map.size() << 2);
            for(Map.Entry<String, String> entry : map.entrySet()) {
                if(entry.getValue() != null) {
                    T obj = transcoder.decode(entry.getValue());
                    resultMap.put(entry.getKey(), obj);
                }
            }
            return resultMap;
        }
        return Collections.emptyMap();
    }

    @Override
    protected <T> Void doAsyncMultiGet(StoreCategoryConfig categoryConfig, List<String> finalKeyList,
                                       final StoreCallback<Map<String, T>> callback) throws Exception {
        JedisCallback<Map<String, String>> jedisCallback = new JedisCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> strMap) {
                Map<String, T> objMap = new HashMap<String, T>(strMap.size() << 2);
                for(Map.Entry<String, String> entry : strMap.entrySet()) {
                    if(entry.getValue() != null) {
                        T obj = transcoder.decode(entry.getValue());
                        objMap.put(entry.getKey(), obj);
                    }
                }
                callback.onSuccess(objMap);
            }

            @Override
            public void onFailure(Throwable ex) {
                callback.onFailure(ex);
            }
            
        };
        clientManager.getClient().asyncMget(jedisCallback, finalKeyList.toArray(new String[0]));
        return null;
    }

    @Override
    protected <T> Boolean doMultiSet(StoreCategoryConfig categoryConfig, List<String> finalKeyList, 
                                     List<T> values) throws Exception {
        Map<String, String> map = new HashMap<String, String>(finalKeyList.size() << 2);
        for(int i=0; i<finalKeyList.size(); i++) {
            String str = transcoder.encode(values.get(i));
            map.put(finalKeyList.get(i), str);
        }
        clientManager.getClient().mset(map);
        return Boolean.TRUE;
    }

    @Override
    protected <T> Void doAsyncMultiSet(StoreCategoryConfig categoryConfig, List<String> keys, List<T> values,
                                    final StoreCallback<Boolean> callback) {
        Map<String, String> map = new HashMap<String, String>(keys.size() << 2);
        for(int i=0; i<keys.size(); i++) {
            String str = transcoder.encode(values.get(i));
            map.put(keys.get(i), str);
        }
        
        JedisCallback<Map<String, String>> jedisCallback = new JedisCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> result) {
                callback.onSuccess(Boolean.TRUE);
            }

            @Override
            public void onFailure(Throwable ex) {
                callback.onFailure(ex);
            }
            
        };
        clientManager.getClient().asyncMset(jedisCallback, map);
        return null;
    }
    
    @Override
    public Boolean exists(StoreKey key) {
        if (key == null) {
            throw new IllegalArgumentException("store key is null");
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().exists(finalKey);
            }
            
        }, categoryConfig, finalKey, "exists");
    }

    @Override
    public String type(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().type(finalKey);
            }
            
        }, categoryConfig, finalKey, "type");
    }

    @Override
    public Boolean expire(StoreKey key, final int seconds) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = clientManager.getClient().expire(finalKey, seconds);
                return result == 1;
            }
            
        }, categoryConfig, finalKey, "expire");
    }

    @Override
    public Long ttl(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = clientManager.getClient().ttl(finalKey);
                return result;
            }
            
        }, categoryConfig, finalKey, "ttl");
    }

    @Override
    public Boolean persist(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = clientManager.getClient().persist(finalKey);
                return result == 1;
            }
            
        }, categoryConfig, finalKey, "persist");
    }

    @Override
    public Long hset(StoreKey key, final String field, final Object value) {
        checkNotNull(key, "store key is null");
        checkNotNull(field, "hash field is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String serialized = transcoder.encode(value);
                Long result = clientManager.getClient().hset(finalKey, field, serialized);
                return result;
            }
            
        }, categoryConfig, finalKey, "hset");
    }

    @Override
    public <T> T hget(StoreKey key, final String field) {
        checkNotNull(key, "store key is null");
        checkNotNull(field, "hash field is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().hget(finalKey, field);
                if(value != null) {
                    T object = transcoder.decode(value);
                    return object;
                } else {
                    return null;
                }
            }
            
        }, categoryConfig, finalKey, "hget");
    }
    
    @Override
    public List<Object> hmget(StoreKey key, final String... fields) {
        checkNotNull(key, "store key is null");
        if(fields == null || fields.length == 0) {
            return null;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> values = clientManager.getClient().hmget(finalKey, fields);
                if(values != null) {
                    List<Object> objects = new ArrayList<Object>(values.size());
                    for(String value : values) {
                        if(value != null) {
                            Object object = transcoder.decode(value);
                            objects.add(object);
                        } else {
                            objects.add(null);
                        }
                    }
                    return objects;
                } else {
                    return null;
                }
            }
            
        }, categoryConfig, finalKey, "hmget");
    }

    @Override
    public Boolean hmset(StoreKey key, final Map<String, Object> objMap) {
        checkNotNull(key, "store key is null");
        checkNotNull(objMap, "hash values are null");
        if(objMap.size() == 0) {
            return true;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Map<String, String> strMap = new HashMap<String, String>(objMap.size());
                for(Map.Entry<String, Object> entry : objMap.entrySet()) {
                    if(entry.getValue() != null) {
                        String str = transcoder.encode(entry.getValue());
                        strMap.put(entry.getKey(), str);
                    }
                }
                String result = clientManager.getClient().hmset(finalKey, strMap);
                return "OK".equals(result);
            }
            
        }, categoryConfig, finalKey, "hmset");
    }

    @Override
    public Long hdel(StoreKey key, final String... fields) {
        checkNotNull(key, "store key is null");
        if(fields == null || fields.length == 0) {
            return 0L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().hdel(finalKey, fields);
            }
            
        }, categoryConfig, finalKey, "hdel");
    }

    @Override
    public Set<String> hkeys(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().hkeys(finalKey);
            }
            
        }, categoryConfig, finalKey, "hkeys");
    }

    @Override
    public List<Object> hvals(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> values = clientManager.getClient().hvals(finalKey);
                if (values != null && values.size() > 0) {
                    List<Object> objects = new ArrayList<Object>(values.size());
                    for (String value : values) {
                        Object object = transcoder.decode(value);
                        objects.add(object);
                    }
                    return objects;
                } else {
                    return Collections.EMPTY_LIST;
                }
            }
            
        }, categoryConfig, finalKey, "hvals");
    }

    @Override
    public Map<String, Object> hgetAll(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Map<String, String> map = clientManager.getClient().hgetAll(finalKey);
                if (map != null && map.size() > 0) {
                    Map<String, Object> objMap = new HashMap<String, Object>(map.size());
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        Object object = transcoder.decode(entry.getValue());
                        objMap.put(entry.getKey(), object);
                    }
                    return objMap;
                } else {
                    return Collections.EMPTY_MAP;
                }
            }
            
        }, categoryConfig, finalKey, "hgetAll");
    }

    @Override
    public Long hincrBy(StoreKey key, final String field, final int amount) {
        checkNotNull(key, "store key is null");
        checkNotNull(field, "hash field is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().hincrBy(finalKey, field, amount);
            }
            
        }, categoryConfig, finalKey, "hincrBy");
    }
    
    @Override
    public Long rpush(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return -1L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String[] strings = new String[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    if (object == null) {
                        throw new IllegalArgumentException("one of the list values is null");
                    }
                    strings[i] = transcoder.encode(object);
                }
                return clientManager.getClient().rpush(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "rpush");
    }

    @Override
    public Long lpush(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return -1L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String[] strings = new String[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    if (object == null) {
                        throw new IllegalArgumentException("one of the list values is null");
                    }
                    strings[i] = transcoder.encode(object);
                }
                return clientManager.getClient().lpush(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "lpush");
    }

    @Override
    public <T> T lpop(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().lpop(finalKey);
                if (value != null) {
                    T object = transcoder.decode(value);
                    return object;
                }
                return null;
            }
            
        }, categoryConfig, finalKey, "lpop");
    }

    @Override
    public <T> T rpop(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().rpop(finalKey);
                if (value != null) {
                    T object = transcoder.decode(value);
                    return object;
                }
                return null;
            }
            
        }, categoryConfig, finalKey, "rpop");
    }

    @Override
    public <T> T lindex(StoreKey key, final long index) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().lindex(finalKey, index);
                if (value != null) {
                    T object = transcoder.decode(value);
                    return object;
                }
                return null;
            }
            
        }, categoryConfig, finalKey, "lindex");
    }

    @Override
    public Boolean lset(StoreKey key, final long index, final Object object) {
        checkNotNull(key, "store key is null");
        checkNotNull(key, "value is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = transcoder.encode(object);
                if (value != null) {
                    String result = clientManager.getClient().lset(finalKey, index, value);
                    return "OK".equals(result);
                }
                return false;
            }
            
        }, categoryConfig, finalKey, "lset");
    }

    @Override
    public Long llen(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().llen(finalKey);
            }
            
        }, categoryConfig, finalKey, "llen");
    }

    @Override
    public List<Object> lrange(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> strList = clientManager.getClient().lrange(finalKey, start, end);
                if (strList != null && strList.size() > 0) {
                    List<Object> objList = new ArrayList<Object>(strList.size());
                    for (String str : strList) {
                        Object obj = transcoder.decode(str);
                        objList.add(obj);
                    }
                    return objList;
                } else {
                    return Collections.EMPTY_LIST;
                }
            }
            
        }, categoryConfig, finalKey, "lrange");
    }

    @Override
    public Boolean ltrim(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String result = clientManager.getClient().ltrim(finalKey, start, end);
                return "OK".equals(result);
            }
            
        }, categoryConfig, finalKey, "ltrim");
    }

    @Override
    public Long sadd(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return 0L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                if (objects != null && objects.length > 0) {
                    String[] strings = new String[objects.length];
                    for (int i = 0; i < objects.length; i++) {
                        Object object = objects[i];
                        if (object == null) {
                            throw new IllegalArgumentException("one of the set values is null");
                        }
                        strings[i] = transcoder.encode(object);
                    }
                    return clientManager.getClient().sadd(finalKey, strings);
                } else {
                    return -1L;
                }
            }
            
        }, categoryConfig, finalKey, "sadd");
    }

    @Override
    public Long srem(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return 0L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String[] strings = new String[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    Object object = objects[i];
                    if (object == null) {
                        throw new IllegalArgumentException("one of the set values is null");
                    }
                    strings[i] = transcoder.encode(object);
                }
                return clientManager.getClient().srem(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "srem");
    }

    @Override
    public Set<Object> smembers(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().smembers(finalKey);
                if (strSet != null && strSet.size() > 0) {
                    Set<Object> objSet = new HashSet<Object>(strSet.size());
                    for (String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                } else {
                    return Collections.EMPTY_SET;
                }
            }
            
        }, categoryConfig, finalKey, "smembers");
    }

    @Override
    public Long scard(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().scard(finalKey);
            }
            
        }, categoryConfig, finalKey, "scard");
    }

    @Override
    public Boolean sismember(StoreKey key, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "set member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().sismember(finalKey, str);
            }
            
        }, categoryConfig, finalKey, "sismember");
    }
    
    @Override
    public Object spop(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().spop(finalKey);
                if(value != null) {
                    return transcoder.decode(value);
                }
                return null;
            }
            
        }, categoryConfig, finalKey, "spop");
    }

    @Override
    public Set<Object> spop(StoreKey key, final long count) {
        checkNotNull(key, "store key is null");
        checkArgument(count > 0, "count is less then or equal to 0");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> set = clientManager.getClient().spop(finalKey, count);
                if(set != null) {
                    Set<Object> result = new HashSet<Object>(set.size());
                    for(String item : set) {
                        result.add(transcoder.decode(item));
                    }
                    return result;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "spop");
    }

    @Override
    public Object srandmember(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = clientManager.getClient().srandmember(finalKey);
                if(value != null) {
                    return transcoder.decode(value);
                }
                return null;
            }
            
        }, categoryConfig, finalKey, "srandmember");
    }

    @Override
    public List<Object> srandmember(StoreKey key, final int count) {
        checkNotNull(key, "store key is null");
        checkArgument(count > 0, "count is less then or equal to 0");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> list = clientManager.getClient().srandmember(finalKey, count);
                if(list != null) {
                    List<Object> result = new ArrayList<Object>(list.size());
                    for(String item : list) {
                        result.add(transcoder.decode(item));
                    }
                    return result;
                }
                return Collections.EMPTY_LIST;
            }
            
        }, categoryConfig, finalKey, "srandmember");
    }

    @Override
    public Long zadd(StoreKey key, final double score, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "zset member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().zadd(finalKey, score, str);
            }
            
        }, categoryConfig, finalKey, "zadd");
    }

    @Override
    public Long zadd(StoreKey key, final Map<Object, Double> scoreMembers) {
        checkNotNull(key, "store key is null");
        checkNotNull(scoreMembers, "zset member map is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Map<String, Double> zsetMap = new HashMap<String, Double>(scoreMembers.size());
                for(Map.Entry<Object, Double> entry : scoreMembers.entrySet()) {
                    String str = transcoder.encode(entry.getKey());
                    zsetMap.put(str, entry.getValue());
                }
                return clientManager.getClient().zadd(finalKey, zsetMap);
            }
            
        }, categoryConfig, finalKey, "zadd");
    }

    @Override
    public Long zrem(StoreKey key, final Object... members) {
        checkNotNull(key, "store key is null");
        if(members.length == 0) {
            return 0L;
        }
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String[] memberStrs = new String[members.length];
                for(int i=0; i<members.length; i++) {
                    String str = transcoder.encode(members[i]);
                    memberStrs[i] = str;
                }
                return clientManager.getClient().zrem(finalKey, memberStrs);
            }
            
        }, categoryConfig, finalKey, "zrem");
    }

    @Override
    public Double zincrby(StoreKey key, final double score, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "zset member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().zincrby(finalKey, score, str);
            }
            
        }, categoryConfig, finalKey, "zincrby");
    }

    @Override
    public Long zrank(StoreKey key, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "zset member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().zrank(finalKey, str);
            }
            
        }, categoryConfig, finalKey, "zrank");
    }

    @Override
    public Long zrevrank(StoreKey key, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "zset member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().zrevrank(finalKey, str);
            }
            
        }, categoryConfig, finalKey, "zrevrank");
    }

    @Override
    public Long zcard(StoreKey key) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().zcard(finalKey);
            }
            
        }, categoryConfig, finalKey, "zcard");
    }

    @Override
    public Double zscore(StoreKey key, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "zset member is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return clientManager.getClient().zscore(finalKey, str);
            }
            
        }, categoryConfig, finalKey, "zscore");
    }

    @Override
    public Long zcount(StoreKey key, final double min, final double max) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return clientManager.getClient().zcount(finalKey, min, max);
            }
            
        }, categoryConfig, finalKey, "zcount");
    }

    @Override
    public Set<Object> zrange(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrange(finalKey, start, end);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrange");
    }

    @Override
    public Set<Object> zrangeByScore(StoreKey key, final double min, final double max) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrangeByScore(finalKey, min, max);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrangeByScore");
    }

    @Override
    public Set<Object> zrangeByScore(StoreKey key, final double min, final double max, final int offset, final int count) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrangeByScore(finalKey, min, max, offset, count);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrangeByScore");
    }

    @Override
    public Set<Object> zrevrange(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrevrange(finalKey, start, end);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrevrange");
    }

    @Override
    public Set<Object> zrevrangeByScore(StoreKey key, final double max, final double min) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrevrangeByScore(finalKey, min, max);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrevrangeByScore");
    }

    @Override
    public Set<Object> zrevrangeByScore(StoreKey key, final double max, final double min, final int offset, final int count) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = clientManager.getClient().zrevrangeByScore(finalKey, min, max, offset, count);
                if(strSet != null) {
                    Set<Object> objSet = new LinkedHashSet<Object>(strSet.size());
                    for(String str : strSet) {
                        Object obj = transcoder.decode(str);
                        objSet.add(obj);
                    }
                    return objSet;
                }
                return Collections.EMPTY_SET;
            }
            
        }, categoryConfig, finalKey, "zrevrangeByScore");
    }

    @Override
    public Long zremrangeByRank(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return  clientManager.getClient().zremrangeByRank(finalKey, start, end);
            }
            
        }, categoryConfig, finalKey, "zremrangeByRank");
    }

    @Override
    public Long zremrangeByScore(StoreKey key, final double start, final double end) {
        checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return  clientManager.getClient().zremrangeByScore(finalKey, start, end);
            }
            
        }, categoryConfig, finalKey, "zremrangeByScore");
    }

    @Override
    public String getScheme() {
        return "redis-cluster";
    }

    @Override
    public String locate(StoreKey storeKey) {
        String finalKey = getFinalKey(storeKey);
        return locate(finalKey);
    }

    @Override
    public String locate(String finalKey) {
        checkNotNull(finalKey, "final key is null");
        return clientManager.getClient().getClusterNode(finalKey);
    }

    @Override
    public JedisCluster getJedisClient() {
        return clientManager.getClient();
    }

}