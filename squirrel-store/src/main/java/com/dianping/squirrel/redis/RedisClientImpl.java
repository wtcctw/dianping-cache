package com.dianping.squirrel.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

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
import com.dianping.squirrel.exception.StoreException;
import com.dianping.squirrel.serialize.StoreSerializeException;
import com.dianping.squirrel.serialize.Serializer;
import com.dianping.squirrel.serialize.SerializerFactory;

public class RedisClientImpl implements RedisStore, CacheClient, Lifecycle, KeyAware, InitialConfiguration {

    private static Logger logger = LoggerFactory.getLogger(RedisClientImpl.class);
    
    private static final String OK = "OK";
    
    private String key;

    private RedisClientConfig config;
    
    private JedisCluster client;
    
    private Transcoder<String> transcoder = new RedisTranscoder();
    
    private Serializer serializer = SerializerFactory.getSerializer("hessian");
    
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

	@Override
	public Boolean exists(String key) {
		return client.exists(key);
	}

	@Override
	public String type(String key) {
		return client.type(key);
	}

	@Override
	public Boolean expire(String key, int seconds) {
		Long result = client.expire(key, seconds);
		return result == 1;
	}

	@Override
	public Long ttl(String key) {
		Long result = client.ttl(key);
		if(result == -1) 
			throw new StoreException("key " + key + " does not exist");
		if(result == -2) 
			throw new StoreException("key " + key + " exists but has no associated expire");
		return result;
	}

	@Override
	public Boolean persist(String key) {
		Long result = client.persist(key);
		return result == 1;
	}

	@Override
	public Long hset(String key, String field, Object value) {
		try {
			String serialized = serializer.toString(value);
			Long result = client.hset(key, field, serialized);
			return result;
		} catch (StoreSerializeException e) {
			throw new StoreException(e);
		}
	}

	@Override
	public <T> T hget(String key, String field) {
		String value = client.hget(key, field);
		T object = (T)serializer.fromString(value, Object.class);
		return object;
	}

	@Override
	public Long hdel(String key, String... field) {
		return client.hdel(key, field);
	}

	@Override
	public Set<String> hkeys(String key) {
		return client.hkeys(key);
	}

	@Override
	public List<Object> hvals(String key) {
		List<String> values = client.hvals(key);
		if(values != null && values.size() > 0) {
			List<Object> objects = new ArrayList<Object>(values.size());
			for(String value : values) {
				Object object = serializer.fromString(value, Object.class);
				objects.add(object);
			}
			return objects;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public Map<String, Object> hgetAll(String key) {
		Map<String, String> map = client.hgetAll(key);
		if(map != null && map.size() > 0) {
			Map<String, Object> objMap = new HashMap<String, Object>(map.size());
			for(Map.Entry<String, String> entry : map.entrySet()) {
				Object object = serializer.fromString(entry.getValue(), Object.class);
				objMap.put(entry.getKey(), object);
			}
			return objMap;
		} else {
			return Collections.EMPTY_MAP;
		}
	}

	@Override
	public Long rpush(String key, Object... objects) {
		if(objects != null && objects.length > 0) {
			String[] strings = new String[objects.length];
			for(int i=0; i<objects.length; i++) {
				Object object = objects[i];
				if(object == null) {
					throw new IllegalArgumentException("one of the list objects is null");
				}
				strings[i] = serializer.toString(object);
			}
			return client.rpush(key, strings);
		} else {
			return -1L;
		}
	}

	@Override
	public Long lpush(String key, Object... objects) {
		if(objects != null && objects.length > 0) {
			String[] strings = new String[objects.length];
			for(int i=0; i<objects.length; i++) {
				Object object = objects[i];
				if(object == null) {
					throw new IllegalArgumentException("one of the list objects is null");
				}
				strings[i] = serializer.toString(object);
			}
			return client.lpush(key, strings);
		} else {
			return -1L;
		}
	}

	@Override
	public <T> T lpop(String key) {
		String value = client.lpop(key);
		if(value != null) {
			T object = (T)serializer.fromString(value, Object.class);
			return object;
		}
		return null;
	}

	@Override
	public <T> T rpop(String key) {
		String value = client.rpop(key);
		if(value != null) {
			T object = (T)serializer.fromString(value, Object.class);
			return object;
		}
		return null;
	}

	@Override
	public <T> T lindex(String key, long index) {
		String value = client.lindex(key, index);
		if(value != null) {
			T object = (T)serializer.fromString(value, Object.class);
			return object;
		}
		return null;
	}

	@Override
	public Boolean lset(String key, long index, Object object) {
		String value = serializer.toString(object);
		if(value != null) {
			String result = client.lset(key, index, value);
			return "OK".equals(result);
		}
		return false;
	}

	@Override
	public Long llen(String key) {
		return client.llen(key);
	}

	@Override
	public List<Object> lrange(String key, long start, long end) {
		List<String> strList = client.lrange(key, start, end);
		if(strList != null && strList.size() > 0) {
			List<Object> objList = new ArrayList<Object>(strList.size());
			for(String str : strList) {
				Object obj = serializer.fromString(str, Object.class);
				objList.add(obj);
			}
			return objList;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public Boolean ltrim(String key, long start, long end) {
		String result = client.ltrim(key, start, end);
		return "OK".equals(result);
	}

	@Override
	public Long sadd(String key, Object... objects) {
		if(objects != null && objects.length > 0) {
			String[] strings = new String[objects.length];
			for(int i=0; i<objects.length; i++) {
				Object object = objects[i];
				if(object == null) {
					throw new IllegalArgumentException("one of the set objects is null");
				}
				strings[i] = serializer.toString(object);
			}
			return client.sadd(key, strings);
		} else {
			return -1L;
		}
	}

	@Override
	public Long srem(String key, Object... objects) {
		if(objects != null && objects.length > 0) {
			String[] strings = new String[objects.length];
			for(int i=0; i<objects.length; i++) {
				Object object = objects[i];
				if(object == null) {
					throw new IllegalArgumentException("one of the set objects is null");
				}
				strings[i] = serializer.toString(object);
			}
			return client.srem(key, strings);
		} else {
			return -1L;
		}
	}

	@Override
	public Set<Object> smembers(String key) {
		Set<String> strSet = client.smembers(key);
		if(strSet != null && strSet.size() > 0) {
			Set<Object> objSet = new HashSet<Object>(strSet.size());
			for(String str : strSet) {
				Object obj = serializer.fromString(str, Object.class);
				objSet.add(obj);
			}
			return objSet;
		} else {
			return Collections.EMPTY_SET;
		}
	}

	@Override
	public Long scard(String key) {
		return client.scard(key);
	}

	@Override
	public Boolean sismember(String key, Object member) {
		if(member == null) 
			throw new IllegalArgumentException("set member is null");
		String str = serializer.toString(member);
		return client.sismember(key, str);
	}

}