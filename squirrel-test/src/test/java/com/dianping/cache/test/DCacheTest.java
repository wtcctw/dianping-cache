package com.dianping.cache.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.cache.CacheServiceFactory;
import com.dianping.squirrel.client.core.CacheCallback;
import com.dianping.squirrel.client.impl.dcache.DCacheClientConfig;
import com.dianping.squirrel.client.impl.dcache.DCacheClientImpl;
import com.dianping.squirrel.client.impl.dcache.HessianTranscoder;

public class DCacheTest {

    String category = "mydcache";
    boolean isHot = false;
    int exp = 0;
    
    @Test
    public void test0() throws Exception {
        final DCacheClientImpl dcache = new DCacheClientImpl();
        DCacheClientConfig config = new DCacheClientConfig();
        config.setClientClazz("com.dianping.cache.memcached.MemcachedClientImpl");
        config.setModule("testimage");
        config.setProxy("DCache.testProxyServer.ProxyObj");
        config.setLocator("taf.tafregistry.QueryObj@tcp -h 192.168.215.157 -p 17890");
        config.setPersistent(true);
        config.setTranscoderClass(HessianTranscoder.class);
        dcache.initialize(config);
        dcache.start();
        
        String value = null;
        boolean result = dcache.set("mydcache.enlight_0", "jijiji", exp, isHot, category);
        value = dcache.get("mydcache.enlight_0", Object.class, category);
        assertEquals(value, "jijiji");
        
        List<String> keys = new ArrayList<String>();
        keys.add("mydcache.a_0");
        keys.add("mydcache.b_0");
        keys.add("mydcache.c_0");
        List<String> values = new ArrayList<String>();
        values.add("aaaaa");
        values.add("bbbbb");
        values.add("ccccc");
        
        final CountDownLatch latch = new CountDownLatch(1);
        dcache.asyncBatchSet(keys, values, exp, isHot, category, new CacheCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                latch.countDown();
            }

            @Override
            public void onFailure(String msg, Throwable e) {
                e.printStackTrace();
            }
            
        });
        
        latch.await();
        value = dcache.get("mydcache.a_0", Object.class, category);
        assertEquals(value, "aaaaa");
        value = dcache.get("mydcache.b_0", Object.class, category);
        assertEquals(value, "bbbbb");
        value = dcache.get("mydcache.c_0", Object.class, category);
        assertEquals(value, "ccccc");
        
        Thread.sleep(exp > 0 ? exp * 2 * 1000 : 1000);
        value = dcache.get("mydcache.a_0", Object.class, category);
        assertEquals(value, exp > 0 ? null : "aaaaa");
        value = dcache.get("mydcache.b_0", Object.class, category);
        assertEquals(value, exp > 0 ? null : "bbbbb");
        value = dcache.get("mydcache.c_0", Object.class, category);
        assertEquals(value, exp > 0 ? null : "ccccc");
    }
    
    @Test
    public void testBasic() throws Exception {
        CacheService cs = CacheServiceFactory.getCacheService();
        CacheKey ck = new CacheKey(category, "enlight");
        String input = "chen";
        cs.set(ck, input);
        String output = cs.get(ck);
        assertEquals(output, input);
    }
    
    @Test
    public void testBatch() throws Exception {
        CacheService cs = CacheServiceFactory.getCacheService();
        
        CacheKey key1 = new CacheKey(category, "aaa");
        CacheKey key2 = new CacheKey(category, "bbb");
        CacheKey key3 = new CacheKey(category, "ccc");
        List<CacheKey> keys = new ArrayList<CacheKey>();
        keys.add(key1); keys.add(key2); keys.add(key3);
        
        List<String> inputs = new ArrayList<String>();
        inputs.add("zsedc"); inputs.add("vgyhn"); inputs.add("mkol.");
        
        cs.batchSet(keys, inputs);
        
        String v1 = cs.get(key1);
        assertEquals(v1, "zsedc");
        String v2 = cs.get(key2);
        assertEquals(v2, "vgyhn");
        String v3 = cs.get(key3);
        assertEquals(v3, "mkol.");
        
        List<String> values = new ArrayList<String>();
        values.add("qwert"); values.add("asdfg"); values.add("zxcvb");
        cs.batchSet(keys, values);
        
        String v4 = cs.get(key1);
        assertEquals(v4, "qwert");
        String v5 = cs.get(key2);
        assertEquals(v5, "asdfg");
        String v6 = cs.get(key3);
        assertEquals(v6, "zxcvb");
    }
    
    @Test
    public void testBatchPerf() throws Exception {
        CacheService cs = CacheServiceFactory.getCacheService();
        int loop = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            List<CacheKey> keys = new ArrayList<CacheKey>();
            List<String> values = new ArrayList<String>();
            for (int j = 0; j < 10; j++) {
                int n = i * 10 + j;
                CacheKey key = new CacheKey(category, n);
                keys.add(key);
                values.add("" + n);
            }
            cs.batchSet(keys, values);
        }
        long span = System.currentTimeMillis() - start;
        System.out.println("time: " + span + "ms");
        
        int n1 = new Random().nextInt(10000);
        String n2 = cs.get(new CacheKey(category, n1));
        assertEquals(n2, ""+n1);
    }
    
    @Test
    public void testAsyncBatch() throws Exception {
        CacheService cs = CacheServiceFactory.getCacheService();
        
        CacheKey key1 = new CacheKey(category, "aaa");
        CacheKey key2 = new CacheKey(category, "bbb");
        CacheKey key3 = new CacheKey(category, "ccc");
        List<CacheKey> keys = new ArrayList<CacheKey>();
        keys.add(key1); keys.add(key2); keys.add(key3);
        
        List<String> inputs = new ArrayList<String>();
        inputs.add("zsedc"); inputs.add("vgyhn"); inputs.add("mkol.");
        
        CacheCallback<Boolean> callback = null;
        cs.asyncBatchSet(keys, inputs, callback);
        
        if(callback == null) {
            Thread.sleep(2000);
        }
        
        String v1 = cs.get(key1);
        assertEquals(v1, "zsedc");
        String v2 = cs.get(key2);
        assertEquals(v2, "vgyhn");
        String v3 = cs.get(key3);
        assertEquals(v3, "mkol.");
        
        final CountDownLatch latch = new CountDownLatch(1);
        callback = new CacheCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                latch.countDown();
            }

            @Override
            public void onFailure(String msg, Throwable e) {
                e.printStackTrace();
            }
            
        };
        
        List<String> values = new ArrayList<String>();
        values.add("qwert"); values.add("asdfg"); values.add("zxcvb");
        cs.asyncBatchSet(keys, values, callback);
        latch.await();
        
        String v4 = cs.get(key1);
        assertEquals(v4, "qwert");
        String v5 = cs.get(key2);
        assertEquals(v5, "asdfg");
        String v6 = cs.get(key3);
        assertEquals(v6, "zxcvb");
    }
    
    @Test
    public void testTimeout() {
        CacheService cs = CacheServiceFactory.getCacheService();
        int loop = 10000;
        for(int i=0; i<loop; i++) {
            CacheKey key = new CacheKey(category, i);
            try {
                cs.set(key, i);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
        for(int i=0; i<loop; i++) {
            CacheKey key = new CacheKey(category, i);
            try {
                System.out.println(cs.get(key));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
