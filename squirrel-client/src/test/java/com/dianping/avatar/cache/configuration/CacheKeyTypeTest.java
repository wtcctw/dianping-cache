package com.dianping.avatar.cache.configuration;

import static org.junit.Assert.*;

import org.junit.Test;

public class CacheKeyTypeTest {

    @Test
    public void testPerformance() {
        CacheKeyType cacheKey = new CacheKeyType();
        cacheKey.setCategory("testCategory");
        cacheKey.setVersion(12);
        cacheKey.setIndexTemplate("a{0}b{1}");
        int loop = 1000000;
        long start = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            String key = cacheKey.getKey("hello", "world");
//            System.out.println(key);
        }
        long span = System.currentTimeMillis() - start;
        System.out.println("getKey took " + span + " ms");
        start = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            String key = cacheKey.getKey2("hello", "world");
//            System.out.println(key);
        }
        span = System.currentTimeMillis() - start;
        System.out.println("getKey2 took " + span + " ms");
        start = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            String key = cacheKey.getKey("hello", "world");
//            System.out.println(key);
        }
        span = System.currentTimeMillis() - start;
        System.out.println("getKey took " + span + " ms");
        start = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            String key = cacheKey.getKey2("hello", "world");
//            System.out.println(key);
        }
        span = System.currentTimeMillis() - start;
        System.out.println("getKey2 took " + span + " ms");
    }
    
    @Test
    public void testConsistency() {
        CacheKeyType cacheKey = new CacheKeyType();
        cacheKey.setCategory("testCategory");
        cacheKey.setVersion(12);
        String key = cacheKey.getKey();
        String key2 = cacheKey.getKey2();
        System.out.println(key2);
        assertEquals(key, key2);
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{0}");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{0}{0}");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{0}{1}");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{1}");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{0}");
        key = cacheKey.getKey("he l lo");
        key2 = cacheKey.getKey2("he l lo");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setIndexTemplate("template{1}{2}");
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
        cacheKey.setCategory(null);
        key = cacheKey.getKey("hello");
        key2 = cacheKey.getKey2("hello");
        System.out.println(key2);
        assertEquals(key, key2);
    }

}
