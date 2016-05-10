package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.Bean;
import com.dianping.squirrel.client.impl.User;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings({"rawtypes","deprecation"})
public class RedisStoreClientImplTest {
    
    private static final String STORE_TYPE = "redis-default";
    
    private static final String CATEGORY = "myredis";
    
    @Test
    public void testCommon() throws InterruptedException {
		RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "string");
        Object value = redisClient.delete(key);
        value = redisClient.ttl(key);
        assertEquals(value, -2L);
        value = redisClient.exists(key);
        assertEquals(value, Boolean.FALSE);
        redisClient.add(key, "string");
        value = redisClient.exists(key);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.ttl(key);
        assertEquals(value, -1L);
        value = redisClient.expire(key, 60);
        assertEquals(value, Boolean.TRUE);
        Thread.sleep(2000);
        value = redisClient.ttl(key);
        assertTrue((Long)value < 60L);
        value = redisClient.persist(key);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.ttl(key);
        assertEquals(value, -1L);
        value = redisClient.type(key);
        assertEquals(value, "string");
        value = redisClient.set(key, 1000L);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.increase(key, 1000);
        assertEquals(value, 2000L);
        value = redisClient.get(key);
        assertEquals(value, Long.valueOf(2000));
    }
    
    @Test
    public void testExists() {
        fail("Not yet implemented");
    }

    @Test
    public void testType() {
        fail("Not yet implemented");
    }

    @Test
    public void testExpire() {
        fail("Not yet implemented");
    }

    @Test
    public void testTtl() {
        fail("Not yet implemented");
    }

    @Test
    public void testPersist() {
        fail("Not yet implemented");
    }

	@Test
    public void testHash() {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "hash");
        Bean v1 = new Bean(1, "b1");
        Bean v2 = new Bean(2, "b2");
        Bean v3 = new Bean(3, "b3");
        redisClient.delete(key);
        Object value = redisClient.hdel(key, "field");
        value = redisClient.hset(key, "field", v1);
        assertEquals(value, 1L);
        value = redisClient.hset(key, "field", v2);
        assertEquals(value, 0L);
        value = redisClient.hget(key, "field");
        assertEquals(value, v2);
        value = redisClient.hdel(key, "field");
        assertEquals(value, 1L);
        value = redisClient.hget(key, "field");
        assertEquals(value, null);
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 0);
        value = redisClient.hset(key, "f1", v1);
        value = redisClient.hset(key, "f2", v2);
        value = redisClient.hset(key, "f3", v3);
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 3);
        value = redisClient.hvals(key);
        assertEquals(((List)value).size(), 3);
        value = redisClient.hgetAll(key);
        assertEquals(((Map)value).size(), 3);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("f4", v1);
        values.put("f5", v2);
        values.put("f6", v3);
        value = redisClient.hmset(key, values);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.hmget(key, "f1", "f3", "f5");
        assertEquals(((List)value).size(), 3);
    }
    
    @Test
    public void testHashUser() {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "user");
        User u1 = new User("user1", "city1", "dpid1", "bankcard1");
        User u2 = new User("user2", "city2", "dpid2", "bankcard2");
        User u3 = new User("user3", "city3", "dpid3", "bankcard3");
        User u4 = new User("user4", "city4", "dpid4", "bankcard4");
        User u5 = new User("user5", "city5", "dpid5", "bankcard5");
        User u6 = new User("user6", "city6", "dpid6", "bankcard6");
        
        redisClient.delete(key);
        Object value = redisClient.hdel(key, "field");
        value = redisClient.hset(key, "field", u1);
        assertEquals(value, 1L);
        value = redisClient.hset(key, "field", u2);
        assertEquals(value, 0L);
        value = redisClient.hget(key, "field");
        assertEquals(value, u2);
        value = redisClient.hdel(key, "field");
        assertEquals(value, 1L);
        value = redisClient.hget(key, "field");
        assertEquals(value, null);
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 0);
        value = redisClient.hset(key, "f1", u1);
        value = redisClient.hset(key, "f2", u2);
        value = redisClient.hset(key, "f3", u3);
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 3);
        value = redisClient.hvals(key);
        assertEquals(((List)value).size(), 3);
        value = redisClient.hgetAll(key);
        assertEquals(((Map)value).size(), 3);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("f4", u4);
        values.put("f5", u5);
        values.put("f6", u6);
        value = redisClient.hmset(key, values);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.hmget(key, "f1", "f3", "f5");
        assertEquals(((List)value).size(), 3);
    }
    
    @Test
    public void testHashString() {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "hash");
        redisClient.delete(key);
        Object value = redisClient.hdel(key, "field");
        value = redisClient.hset(key, "field", "value1");
        assertEquals(value, 1L);
        value = redisClient.hset(key, "field", "value2");
        assertEquals(value, 0L);
        value = redisClient.hget(key, "field");
        assertEquals(value, "value2");
        value = redisClient.hdel(key, "field");
        assertEquals(value, 1L);
        value = redisClient.hget(key, "field");
        assertEquals(value, null);
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 0);
        value = redisClient.hset(key, "f1", "v1");
        value = redisClient.hset(key, "f2", "v2");
        value = redisClient.hset(key, "f3", "v3");
        value = redisClient.hkeys(key);
        assertEquals(((Set)value).size(), 3);
        value = redisClient.hvals(key);
        assertEquals(((List)value).size(), 3);
        value = redisClient.hgetAll(key);
        assertEquals(((Map)value).size(), 3);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("f4", "v4");
        values.put("f5", "v5");
        values.put("f6", "v6");
        value = redisClient.hmset(key, values);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.hmget(key, "f1", "f3", "f5");
        assertEquals(((List)value).size(), 3);
    }
    
    @Test
    public void testHset() {
        fail("Not yet implemented");
    }

    @Test
    public void testHget() {
        fail("Not yet implemented");
    }

    @Test
    public void testHdel() {
        fail("Not yet implemented");
    }

    @Test
    public void testHkeys() {
        fail("Not yet implemented");
    }

    @Test
    public void testHvals() {
        fail("Not yet implemented");
    }

    @Test
    public void testHgetAll() {
        fail("Not yet implemented");
    }

    @Test
    public void testList() {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "list");
        redisClient.delete(key);
        Object value = redisClient.llen(key);
        assertEquals(value, 0L);
        value = redisClient.lpush(key, "v1", "v2", "v3");
        assertEquals(value, 3L);
        value = redisClient.rpop(key);
        assertEquals(value, "v1");
        value = redisClient.llen(key);
        assertEquals(value, 2L);
        value = redisClient.lpop(key);
        assertEquals(value, "v3");
        value = redisClient.rpush(key, "v4", "v5", "v6");
        assertEquals(value, 4L);
        value = redisClient.lindex(key, 0);
        assertEquals(value, "v2");
        value = redisClient.lindex(key, -1);
        assertEquals(value, "v6");
        value = redisClient.lset(key, 1, "v7");
        assertEquals(value, Boolean.TRUE);
        value = redisClient.lrange(key, 0, -1);
        assertEquals(((List)value).size(), 4);
        value = redisClient.ltrim(key, 0, 2);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.llen(key);
        assertEquals(value, 3L);
        value = redisClient.lindex(key, 1);
        assertEquals(value, "v7");
    }
    
    @Test
    public void testRpush() {
        fail("Not yet implemented");
    }

    @Test
    public void testLpush() {
        fail("Not yet implemented");
    }

    @Test
    public void testLpop() {
        fail("Not yet implemented");
    }

    @Test
    public void testRpop() {
        fail("Not yet implemented");
    }

    @Test
    public void testLindex() {
        fail("Not yet implemented");
    }

    @Test
    public void testLset() {
        fail("Not yet implemented");
    }

    @Test
    public void testLlen() {
        fail("Not yet implemented");
    }

    @Test
    public void testLrange() {
        fail("Not yet implemented");
    }

    @Test
    public void testLtrim() {
        fail("Not yet implemented");
    }

    @Test
    public void testSets() {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "set");
        redisClient.delete(key);
        Object value = redisClient.scard(key);
        assertEquals(value, 0L);
        value = redisClient.sadd(key, 111, 222, 333);
        assertEquals(value, 3L);
        value = redisClient.srem(key, 444);
        assertEquals(value, 0L);
        value = redisClient.srem(key, 333);
        assertEquals(value, 1L);
        value = redisClient.scard(key);
        assertEquals(value, 2L);
        value = redisClient.sismember(key, 555);
        assertEquals(value, Boolean.FALSE);
        value = redisClient.sismember(key, 111);
        assertEquals(value, Boolean.TRUE);
        value = redisClient.smembers(key);
        assertEquals(((Set)value).size(), 2);
        value = redisClient.srandmember(key);
        assertNotNull(value);
        value = redisClient.spop(key);
        assertNotNull(value);
        value = redisClient.scard(key);
        assertEquals(value, 1L);
    }
    
    @Test
    public void testSadd() {
        fail("Not yet implemented");
    }

    @Test
    public void testSrem() {
        fail("Not yet implemented");
    }

    @Test
    public void testSmembers() {
        fail("Not yet implemented");
    }

    @Test
    public void testScard() {
        fail("Not yet implemented");
    }

    @Test
    public void testSismember() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeleteString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGet() {
        fail("Not yet implemented");
    }

    @Test
    public void testSet() {
        fail("Not yet implemented");
    }

    @Test
    public void testAdd() {
        fail("Not yet implemented");
    }

    @Test
    public void testIncrease() {
        fail("Not yet implemented");
    }

    @Test
    public void testDecrease() {
        fail("Not yet implemented");
    }

    @Test
    public void testZSet() throws Exception {
        RedisStoreClient client = (RedisStoreClient) StoreClientFactory.getStoreClientByCategory(CATEGORY);
        StoreKey key = new StoreKey(CATEGORY, "zset");
        client.delete(key);
        Object result = client.zadd(key, 0.1d, "aaaaa");
        assertEquals(result, 1L);
        result = client.zadd(key, 0.2d, "bbbbb");
        assertEquals(result, 1L);
        result = client.zadd(key, 0.3d, "ccccc");
        assertEquals(result, 1L);
        result = client.zadd(key, 0.31d, "ccccc");
        assertEquals(result, 0L);
        result = client.zcard(key);
        assertEquals(result, 3L);
        result = client.zcount(key, 0.0d, 0.5d);
        assertEquals(result, 3L);
        result = client.zcount(key, 0.1d, 0.3d);
        assertEquals(result, 2L);
        result = client.zincrby(key, 0.01d, "bbbbb");
        assertEquals((Double)result, 0.21d, 0.00001);
        result = client.zscore(key, "bbbbb");
        assertEquals((Double)result, 0.21d, 0.00001);
        result = client.zrank(key, "aaaaa");
        assertEquals(result, 0L);
        result = client.zrank(key, "ccccc");
        assertEquals(result, 2L);
        result = client.zrevrank(key, "ccccc");
        assertEquals(result, 0L);
        result = client.zrank(key, "ddddd");
        assertEquals(result, null);
        result = client.zrem(key, "bbbbb", "ddddd");
        assertEquals(result, 1L);
        result = client.zrange(key, 0, -1);
        assertEquals(((Set)result).size(), 2);
        assertEquals(((Set)result).iterator().next(), "aaaaa");
        result = client.zrangeByScore(key, 0.3d, 0.5d);
        assertEquals(((Set)result).size(), 1);
        assertEquals(((Set)result).iterator().next(), "ccccc");
        result = client.zrangeByScore(key, 0.1d, 0.3d, 0, 1);
        assertEquals(((Set)result).size(), 1);
        assertEquals(((Set)result).iterator().next(), "aaaaa");
        result = client.zrangeByScore(key, 0.1d, 0.3d, 1, 1);
        assertEquals(((Set)result).size(), 0);
        //
        result = client.zrevrange(key, 0, -1);
        assertEquals(((Set)result).size(), 2);
        assertEquals(((Set)result).iterator().next(), "ccccc");
        result = client.zrevrangeByScore(key, 0.3d, 0.5d);
        assertEquals(((Set)result).size(), 1);
        assertEquals(((Set)result).iterator().next(), "ccccc");
        result = client.zrevrangeByScore(key, 0.1d, 0.3d, 0, 1);
        assertEquals(((Set)result).size(), 1);
        assertEquals(((Set)result).iterator().next(), "aaaaa");
        result = client.zrangeByScore(key, 0.1d, 0.3d, 1, 1);
        assertEquals(((Set)result).size(), 0);
    }

    @Test
    public void testRawCommand(){
        RedisStoreClient client = (RedisStoreClient) StoreClientFactory.getStoreClientByCategory(CATEGORY);
        StoreKey key = new StoreKey(CATEGORY, "raw");
        client.delete(key);

        client.setRaw(key,"abc^&*(sdfasfw3",0);
        assertEquals("abc^&*(sdfasfw3",client.getRaw(key));

        client.append(key,"def");
        assertEquals("abc^&*(sdfasfw3def",client.getRaw(key));

        client.setBit(key,1,true);
        assertEquals("abc^&*(sdfasfw3def",client.getRaw(key));


    }


    @Test
    public void test(){
        RedisStoreClient client = (RedisStoreClient) StoreClientFactory.getStoreClientByCategory(CATEGORY);
        StoreKey key = new StoreKey(CATEGORY, "bean");
//        Bean bean = new Bean(12345678, "paasbean");
//        Map<String, Bean> map = new HashMap<String, Bean>();
//        map.put("bean",bean);
//        client.delete(key);
//        client.hset(key,"bean",bean);
        Map<String, Object> mapb = client.hgetAll(key);
        for (Map.Entry<String, Object> entry : mapb.entrySet()) {
            System.out.println(entry.getValue());
        }
//        System.out.println("1");
//        System.out.println(bean);
//        System.out.println(bean1);
    }
}
