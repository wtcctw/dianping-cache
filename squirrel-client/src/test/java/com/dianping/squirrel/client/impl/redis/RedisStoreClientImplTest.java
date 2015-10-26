package com.dianping.squirrel.client.impl.redis;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;

public class RedisStoreClientImplTest {
    
    @Test
    public void testCommon() throws InterruptedException {
        RedisStoreClient redisClient = (RedisStoreClient) StoreClientFactory.getStoreClient("redis-hua");
        StoreKey key = new StoreKey("myredis", "hua");
        Object value = redisClient.delete(key);
        value = redisClient.ttl(key);
        assertEquals(value, -2L);
        value = redisClient.exists(key);
        assertEquals(value, Boolean.FALSE);
        redisClient.add(key, "hua");
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
    }
    
    @Test
    public void testList() {
        
    }
    
    @Test
    public void testSets() {
        
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
