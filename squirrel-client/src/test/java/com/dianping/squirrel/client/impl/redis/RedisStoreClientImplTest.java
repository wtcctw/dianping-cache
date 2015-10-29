package com.dianping.squirrel.client.impl.redis;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;

public class RedisStoreClientImplTest {
    
    @Test
    public void testCommon() throws InterruptedException {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "string");
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
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "hash");
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
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "user");
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
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "hash");
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
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "list");
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
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "set");
        redisClient.delete(key);
        Object value = redisClient.scard(key);
        assertEquals(value, 0L);
        value = redisClient.sadd(key, "s1", "s2", "s3");
        assertEquals(value, 3L);
        value = redisClient.srem(key, "s4");
        assertEquals(value, 0L);
        value = redisClient.srem(key, "s3");
        assertEquals(value, 1L);
        value = redisClient.scard(key);
        assertEquals(value, 2L);
        value = redisClient.sismember(key, "s5");
        assertEquals(value, Boolean.FALSE);
        value = redisClient.sismember(key, "s1");
        assertEquals(value, Boolean.TRUE);
        value = redisClient.smembers(key);
        assertEquals(((Set)value).size(), 2);
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

}
