package com.dianping.cache.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.cache.CacheServiceFactory;

public class RedisClusterTest {

    private String category = "myredis";
    
    @Test
    public void test() throws Exception {
        CacheService cache = CacheServiceFactory.getCacheService();
        CacheKey key = new CacheKey(category, "abc");
        boolean result = cache.set(key, "abc");
        assertTrue(result);
        String value = cache.get(key);
        assertEquals(value, "abc");
    }

    @Test
    public void testRemove() throws Exception {
        String CATEGORY = "STToAssertion";
        String param = "AAFSsPYAkNKN6Mb0Q6Li8D8gawrtLKua78Wu7mClgOQqlAxz+bp0pP/j";

        CacheService cache = CacheServiceFactory.getCacheService();
        CacheKey key = new CacheKey(CATEGORY, param);
        String finalKey = cache.getFinalKey(key);
        System.out.println(finalKey);
        
        Object value = cache.get(key);
        Object value2 = cache.get(key, finalKey);
        assertEquals(value, value2);
        System.out.println(value);
    }
    
    @Test
    public void testTimeout() throws Exception {
        String CATEGORY = "ab-test";
        String param = "hello";

        CacheService cache = CacheServiceFactory.getCacheService();
        CacheKey key = new CacheKey(CATEGORY, param);
        
        cache.set(key, "world");
        String value = cache.get(key);
        assertEquals(value, "world");
        Thread.sleep(5000);
        value = cache.get(key);
        assertEquals(value, "world");
        Thread.sleep(5000);
        value = cache.get(key);
        assertNull(value);
    }
    
}
