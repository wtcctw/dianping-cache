package com.dianping.cache.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.cache.CacheServiceFactory;

public class EhcacheTest {

    String category = "myehcache";
    
    @Test
    public void test() {
        CacheService cache = CacheServiceFactory.getCacheService();
        CacheKey key = new CacheKey(category, 1);
        Type value = new Type(1, "1");
        cache.add(key, value);
        Type value2 = cache.get(key);
        value2.setValue("changed");
        Type value3 = cache.get(key);
        assertEquals(value2.getValue(), value3.getValue());
    }
}
