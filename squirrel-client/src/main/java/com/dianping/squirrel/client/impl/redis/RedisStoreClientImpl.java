package com.dianping.squirrel.client.impl.redis;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;

import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.core.Lifecycle;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreTypeAware;
import com.dianping.squirrel.client.core.Transcoder;
import com.dianping.squirrel.client.impl.AbstractStoreClient;

public class RedisStoreClientImpl extends AbstractStoreClient implements RedisStoreClient, Lifecycle, StoreTypeAware {

    private static Logger logger = LoggerFactory.getLogger(RedisStoreClientImpl.class);

    private static final String OK = "OK";

    private String storeType;

    private RedisClientConfig config;

    private JedisCluster client;

    private Transcoder<String> transcoder = new RedisStringTranscoder();

    @Override
    public void initialize(StoreClientConfig config) {
        this.config = (RedisClientConfig) config;
    }

    @Override
    public void setStoreType(String key) {
        this.storeType = key;
    }

    @Override
    public String getStoreType() {
        return storeType;
    }

    @Override
    public void start() {
        client = RedisClientFactory.createClient(config);
    }

    @Override
    public void stop() {
        client.close();
    }

    @Override
    protected <T> T doGet(CacheKeyType categoryConfig, String finalKey) {
        String value = client.get(finalKey);
        if(value != null) {
            T object = transcoder.decode(value);
            return object;
        } else {
            return null;
        }
    }

    @Override
    protected Boolean doSet(CacheKeyType categoryConfig, String finalKey, Object value) {
        String result = null;
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0)
            result = client.setex(finalKey, categoryConfig.getDurationSeconds(), str);
        else
            result = client.set(finalKey, str);
        return OK.equals(result);
    }

    @Override
    protected Boolean doAdd(CacheKeyType categoryConfig, String finalKey, Object value) {
        String str = transcoder.encode(value);
        if (categoryConfig.getDurationSeconds() > 0) {
            String result = client.set(finalKey, str, "NX", "EX", categoryConfig.getDurationSeconds());
            return OK.equals(result);
        } else {
            long result = client.setnx(finalKey, str);
            return 1 == result;
        }
    }

    @Override
    protected Boolean doDelete(CacheKeyType categoryConfig, String finalKey) {
         long result = client.del(finalKey);
         return 1 == result;
    }

    @Override
    public Long doIncrease(CacheKeyType categoryConfig, String finalKey, int amount) {
        long result = client.incrBy(finalKey, amount);
        return result;
    }

    @Override
    public Long doDecrease(CacheKeyType categoryConfig, String finalKey, int amount) {
        long result = client.decrBy(finalKey, amount);
        return result;
    }

    @Override
    protected <T> Future<T> doAsyncGet(CacheKeyType categoryConfig, String finalKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Future<Boolean> doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Future<Boolean> doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Future<Boolean> doAsyncDelete(CacheKeyType categoryConfig, String finalKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <T> Void doAsyncGet(CacheKeyType categoryConfig, String finalKey, StoreCallback<T> callback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Void doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value, StoreCallback<Boolean> callback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Void doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value,
                              StoreCallback<Boolean> callback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Void doAsyncDelete(CacheKeyType categoryConfig, String finalKey, StoreCallback<Boolean> callback) {
        // TODO Auto-generated method stub
        return null;
    }
    

    @Override
    protected <T> Map<String, T> doMultiGet(CacheKeyType categoryConfig, List<String> finalKeyList) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <T> Void doAsyncMultiGet(CacheKeyType categoryConfig, List<String> finalKeyList,
                                       StoreCallback<Map<String, T>> callback) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <T> Boolean doMultiSet(CacheKeyType categoryConfig, List<String> finalKeyList, 
                                     List<T> values) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Void doAsyncMultiSet(CacheKeyType categoryConfig, List<String> keys, List<T> values,
                                    StoreCallback<Boolean> callback) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Boolean exists(StoreKey key) {
        if (key == null) {
            throw new IllegalArgumentException("store key is null");
        }
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.exists(finalKey);
            }
            
        }, categoryConfig, finalKey, "exists");
    }

    @Override
    public String type(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.type(finalKey);
            }
            
        }, categoryConfig, finalKey, "type");
    }

    @Override
    public Boolean expire(StoreKey key, final int seconds) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = client.expire(finalKey, seconds);
                return result == 1;
            }
            
        }, categoryConfig, finalKey, "expire");
    }

    @Override
    public Long ttl(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = client.ttl(finalKey);
                return result;
            }
            
        }, categoryConfig, finalKey, "ttl");
    }

    @Override
    public Boolean persist(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Long result = client.persist(finalKey);
                return result == 1;
            }
            
        }, categoryConfig, finalKey, "persist");
    }

    @Override
    public Long hset(StoreKey key, final String field, final Object value) {
        checkNotNull(key, "store key is null");
        checkNotNull(field, "hash field is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String serialized = transcoder.encode(value);
                Long result = client.hset(finalKey, field, serialized);
                return result;
            }
            
        }, categoryConfig, finalKey, "hset");
    }

    @Override
    public <T> T hget(StoreKey key, final String field) {
        checkNotNull(key, "store key is null");
        checkNotNull(field, "hash field is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = client.hget(finalKey, field);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> values = client.hmget(finalKey, fields);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
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
                String result = client.hmset(finalKey, strMap);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.hdel(finalKey, fields);
            }
            
        }, categoryConfig, finalKey, "hdel");
    }

    @Override
    public Set<String> hkeys(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.hkeys(finalKey);
            }
            
        }, categoryConfig, finalKey, "hkeys");
    }

    @Override
    public List<Object> hvals(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> values = client.hvals(finalKey);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Map<String, String> map = client.hgetAll(finalKey);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.hincrBy(finalKey, field, amount);
            }
            
        }, categoryConfig, finalKey, "hincrBy");
    }
    
    @Override
    public Long rpush(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return -1L;
        }
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
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
                return client.rpush(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "rpush");
    }

    @Override
    public Long lpush(StoreKey key, final Object... objects) {
        checkNotNull(key, "store key is null");
        if(objects == null || objects.length == 0) {
            return -1L;
        }
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
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
                return client.lpush(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "lpush");
    }

    @Override
    public <T> T lpop(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = client.lpop(finalKey);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = client.rpop(finalKey);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = client.lindex(finalKey, index);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String value = transcoder.encode(object);
                if (value != null) {
                    String result = client.lset(finalKey, index, value);
                    return "OK".equals(result);
                }
                return false;
            }
            
        }, categoryConfig, finalKey, "lset");
    }

    @Override
    public Long llen(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.llen(finalKey);
            }
            
        }, categoryConfig, finalKey, "llen");
    }

    @Override
    public List<Object> lrange(StoreKey key, final long start, final long end) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                List<String> strList = client.lrange(finalKey, start, end);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String result = client.ltrim(finalKey, start, end);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
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
                    return client.sadd(finalKey, strings);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
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
                return client.srem(finalKey, strings);
            }
            
        }, categoryConfig, finalKey, "srem");
    }

    @Override
    public Set<Object> smembers(StoreKey key) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                Set<String> strSet = client.smembers(finalKey);
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
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return client.scard(finalKey);
            }
            
        }, categoryConfig, finalKey, "scard");
    }

    @Override
    public Boolean sismember(StoreKey key, final Object member) {
        checkNotNull(key, "store key is null");
        checkNotNull(member, "set member is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                String str = transcoder.encode(member);
                return client.sismember(finalKey, str);
            }
            
        }, categoryConfig, finalKey, "sismember");
    }

}