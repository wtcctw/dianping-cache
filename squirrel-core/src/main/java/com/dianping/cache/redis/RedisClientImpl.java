package com.dianping.cache.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;

import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.core.CacheClient;
import com.dianping.cache.core.CacheClientConfiguration;
import com.dianping.cache.core.InitialConfiguration;
import com.dianping.cache.core.KeyAware;
import com.dianping.cache.core.Lifecycle;
import com.dianping.cache.core.Transcoder;
import com.dianping.cache.exception.CacheException;
import com.dianping.cache.serialize.SerializeException;

public class RedisClientImpl implements CacheClient, Lifecycle, KeyAware, InitialConfiguration {

    private static Logger logger = LoggerFactory.getLogger(RedisClientImpl.class);
    
    private static final String OK = "OK";
    
    private String key;

    private RedisClientConfig config;
    
    private JedisCluster client;
    
    private Transcoder<String> transcoder = new RedisTranscoder();
    
    @Override
    public void init(CacheClientConfiguration config) {
        this.config = (RedisClientConfig) config;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void start() {
        client = RedisClientFactory.createClient(config);
    }

    @Override
    public void shutdown() {
        client.close();
    }

    @Override
    public Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
            throws CacheException {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public void asyncSet(String key, Object value, int expiration, boolean isHot, String category,
            CacheCallback<Boolean> callback) {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public boolean set(String key, Object value, int expiration, boolean isHot, String category) throws CacheException,
            TimeoutException {
        return set(key, value, expiration, config.getReadTimeout(), isHot, category);
    }

    @Override
    public boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
            throws CacheException, TimeoutException {
        try {
            String result = null;
            String str = transcoder.encode(value);
            if(expiration > 0) 
                result = client.setex(key, expiration, str);
            else 
                result = client.set(key, str);
            return OK.equals(result);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public boolean add(String key, Object value, int expiration, boolean isHot, String category) throws CacheException,
            TimeoutException {
        return add(key, value, expiration, config.getReadTimeout(), isHot, category);
    }

    @Override
    public boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
            throws CacheException, TimeoutException {
        try {
            String str = transcoder.encode(value);
            if(expiration > 0) {
                String result = client.set(key, str, "NX", "EX", expiration);
                return OK.equals(result);
            } else {
                long result = client.setnx(key, str);
                return 1 == result;
            }
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
            throws CacheException {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public void asyncAdd(String key, Object value, int expiration, boolean isHot, String category,
            CacheCallback<Boolean> callback) {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public void replace(String key, Object value, int expiration, boolean isHot, String category) throws Exception {
        try {
            String str = transcoder.encode(value);
            if(expiration > 0) {
                String result = client.set(key, str, "XX", "EX", expiration);
            } else {
                new UnsupportedOperationException("replace with expiration <=0 is not supported in redis");
            }
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> T get(String key, Class dataType, String category) throws Exception {
        return get(key, dataType, false, category, false);
    }

    @Override
    public <T> T get(String key, Class dataType, boolean isHot, String category, boolean timeoutAware) throws Exception {
        try {
            String value = client.get(key);
            T object = (T)transcoder.decode(value, dataType);
            return object;
        } catch(Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> Future<T> asyncGet(String key, Class dataType, boolean isHot, String category) throws CacheException {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public <T> void asyncGet(String key, Class dataType, boolean isHot, String category, CacheCallback<T> callback) {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public <T> void asyncBatchGet(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
            CacheCallback<Map<String, T>> callback) {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public <T> void asyncBatchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category,
            CacheCallback<Boolean> callback) {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public <T> boolean batchSet(List<String> keys, List<T> values, int expiration, boolean isHot, String category)
            throws CacheException, TimeoutException {
        throw new UnsupportedOperationException("redis does not support batch operations");
    }

    @Override
    public <T> Map<String, T> getBulk(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
            boolean timeoutAware) throws Exception {
        throw new UnsupportedOperationException("redis does not support batch operations");
    }

    @Override
    public Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws CacheException {
        throw new UnsupportedOperationException("redis does not support async operations");
    }

    @Override
    public boolean delete(String key, boolean isHot, String category) throws CacheException, TimeoutException {
        return delete(key, isHot, category, config.getReadTimeout());
    }

    @Override
    public boolean delete(String key, boolean isHot, String category, long timeout) throws CacheException,
            TimeoutException {
        long result = client.del(key);
        return 1 == result;
    }

    @Override
    public long increment(String key, int amount, String category) throws CacheException, TimeoutException {
        long result = client.incrBy(key, amount);
        return result;
    }

    @Override
    public long increment(String key, int amount, String category, long def) throws CacheException, TimeoutException {
        throw new UnsupportedOperationException("redis does not support increment with default value, default value is 0 if the key does not exist");
    }

    @Override
    public long decrement(String key, int amount, String category) throws CacheException, TimeoutException {
        long result = client.decrBy(key, amount);
        return result;
    }

    @Override
    public long decrement(String key, int amount, String category, long def) throws CacheException, TimeoutException {
        throw new UnsupportedOperationException("redis does not support decrement with default value, default value is 0 if the key does not exist");
    }

    @Override
    public void clear() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDistributed() {
        return true;
    }

    @Override
    public <T> CASValue<T> gets(String key, String category) throws CacheException, TimeoutException {
        throw new UnsupportedOperationException("redis does not support cas operation");
    }

    @Override
    public CASResponse cas(String key, long casId, Object value, String category) throws CacheException,
            TimeoutException {
        throw new UnsupportedOperationException("redis does not support cas operation");
    }

}