package com.dianping.squirrel.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.core.StoreCallback;

public class DefaultStoreClientTest extends TestCase {
    
    private static final String[] CATEGORIES = {
            "mymemcache", "mydcache", "myredis", "myehcache", "mydanga"
    };
    
    static String CATEGORY = "testRedis";
    
    private static final String VALUE = "dp@123456";
    
    private static final Bean BEAN = new Bean(12345678, "BEAN@12345678");

    @Test
    public void testGet() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.get(key);
        assertNull(result);
        client.set(key, VALUE);
        result = client.get(key);
        assertEquals(VALUE, result);
        client.set(key, BEAN);
        result = client.get(key);
        assertEquals(BEAN, result);
    }
    
    @Test
    public void testSet() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.set(key, VALUE);
        assertEquals(Boolean.TRUE, result);
        result = client.get(key);
        assertEquals(VALUE, result);
        result = client.set(key, BEAN);
        assertEquals(Boolean.TRUE, result);
        result = client.get(key);
        assertEquals(BEAN, result);
    }

    @Test
    public void testAdd() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.add(key, VALUE);
        assertEquals(Boolean.TRUE, result);
        result = client.add(key, VALUE);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testDelete() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        Object result = client.set(key, VALUE);
        assertEquals(Boolean.TRUE, result);
        result = client.delete(key);
        assertEquals(Boolean.TRUE, result);
        result = client.get(key);
        assertNull(result);
        result = client.delete(key);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testAsyncGetStoreKey() throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        Object result = client.set(key, BEAN);
        assertEquals(Boolean.TRUE, result);
        Future<Bean> future = client.asyncGet(key);
        assertNotNull(future);
        result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(BEAN, result);
    }

    @Test
    public void testAsyncSetStoreKeyObject() throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        Bean bean = new Bean(53559777, "dianping");
        Future<Boolean> future = client.asyncSet(key, bean);
        assertNotNull(future);
        Boolean result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, result);
        Bean bean2 = client.get(key);
        assertEquals(bean, bean2);
    }

    @Test
    public void testAsyncAddStoreKeyObject() throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Bean bean = new Bean(53559777, "dianping");
        Future<Boolean> future = client.asyncAdd(key, bean);
        assertNotNull(future);
        Boolean result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, result);
        Bean bean2 = client.get(key);
        assertEquals(bean, bean2);
        future = client.asyncAdd(key, bean);
        assertNotNull(future);
        result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.FALSE, result);

    }

    @Test
    public void testAsyncDeleteStoreKey() throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.set(key, BEAN);
        Future<Boolean> future = client.asyncDelete(key);
        assertNotNull(future);
        Boolean result = future.get(60000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, result);
        future = client.asyncDelete(key);
        assertNotNull(future);
        result = future.get(60000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testAsyncGetStoreKeyStoreCallbackOfT() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        Object result = client.set(key, BEAN);
        assertEquals(Boolean.TRUE, result);
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Bean> holder = new ResultHolder<Bean>();
        client.asyncGet(key, new StoreCallback<Bean>() {

            @Override
            public void onSuccess(Bean result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
            }
            
        });
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(BEAN, holder.get());
    }

    @Test
    public void testAsyncSetStoreKeyObjectStoreCallbackOfBoolean() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Bean bean = new Bean(53559777, "dianping");
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncSet(key, bean, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.get());
        Bean bean2 = client.get(key);
        assertEquals(bean, bean2);
    }

    @Test
    public void testAsyncAddStoreKeyObjectStoreCallbackOfBoolean() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Bean bean = new Bean(53559777, "dianping");
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncAdd(key, bean, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.get());
        result = client.add(key, BEAN);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testAsyncDeleteStoreKeyStoreCallbackOfBoolean() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        Bean bean = new Bean(53559777, "dianping");
        client.set(key, bean);
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncDelete(key, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
                e.printStackTrace();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.get());
        result = client.delete(key);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testIncrease() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.increase(key, 100);
        assertEquals(100L, result);
        result = client.increase(key, 100);
        assertEquals(200L, result);
    }

    @Test
    public void testDecrease() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.set(key, 1000L);
        Object result = client.decrease(key, 100);
        assertEquals(900L, result);
        result = client.decrease(key, 100);
        assertEquals(800L, result);
    }

    @Test
    public void testMultiGet() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key1 = new StoreKey(CATEGORY, "key1");
        StoreKey key2 = new StoreKey(CATEGORY, "key2");
        StoreKey key3 = new StoreKey(CATEGORY, "key3");
        StoreKey key4 = new StoreKey(CATEGORY, "key4");
        StoreKey key5 = new StoreKey(CATEGORY, "key5");
        StoreKey key6 = new StoreKey(CATEGORY, "key6");
        client.set(key1, new Bean(1, "value1"));
        client.set(key2, new Bean(2, "value2"));
        client.set(key3, new Bean(3, "value3"));
        client.set(key4, new Bean(4, "value4"));
        client.set(key5, new Bean(5, "value5"));
        List<StoreKey> keys = new ArrayList<StoreKey>(5);
        keys.add(key1);
        keys.add(key2);
        keys.add(key6);
        keys.add(key4);
        keys.add(key5);
        Object result = client.multiGet(keys);
        assertEquals(4, ((Map)result).size());
        assertNull(((Map)result).get(key6));
        assertEquals(new Bean(4, "value4"), ((Map)result).get(key4));
    }

    @Test
    public void testAsyncMultiGet() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key1 = new StoreKey(CATEGORY, "key1");
        StoreKey key2 = new StoreKey(CATEGORY, "key2");
        StoreKey key3 = new StoreKey(CATEGORY, "key3");
        StoreKey key4 = new StoreKey(CATEGORY, "key4");
        StoreKey key5 = new StoreKey(CATEGORY, "key5");
        StoreKey key6 = new StoreKey(CATEGORY, "key6");
        client.set(key1, new Bean(1, "value1"));
        client.set(key2, new Bean(2, "value2"));
        client.set(key3, new Bean(3, "value3"));
        client.set(key4, new Bean(4, "value4"));
        client.set(key5, new Bean(5, "value5"));
        List<StoreKey> keys = new ArrayList<StoreKey>(5);
        keys.add(key1);
        keys.add(key2);
        keys.add(key6);
        keys.add(key4);
        keys.add(key5);
        
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Map<StoreKey, Bean>> holder = new ResultHolder<Map<StoreKey, Bean>>();
        client.asyncMultiGet(keys, new StoreCallback<Map<StoreKey, Bean>>() {

            @Override
            public void onSuccess(Map<StoreKey, Bean> result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
            }
            
        });
        latch.await(60, TimeUnit.SECONDS);
        assertEquals(4, holder.get().size());
        assertNull(holder.get().get(key6));
        assertEquals(new Bean(4, "value4"), holder.get().get(key4));
    }

    @Test
    public void testMultiSet() {
        StoreClient client = StoreClientFactory.getStoreClient();
        client.delete(new StoreKey(CATEGORY, "key3"));
        List<StoreKey> keys = new ArrayList<StoreKey>(5);
        keys.add(new StoreKey(CATEGORY, "key1"));
        keys.add(new StoreKey(CATEGORY, "key2"));
        keys.add(new StoreKey(CATEGORY, "key3"));
        keys.add(new StoreKey(CATEGORY, "key4"));
        keys.add(new StoreKey(CATEGORY, "key5"));
        List<Bean> values = new ArrayList<Bean>(5);
        values.add(new Bean(1, "value1"));
        values.add(new Bean(2, "value2"));
        values.add(new Bean(3, "value3"));
        values.add(new Bean(4, "value4"));
        values.add(new Bean(5, "value5"));
        Object result = client.multiSet(keys, values);
        assertEquals(result, true);
        result = client.get(new StoreKey(CATEGORY, "key3"));
        assertEquals(result, new Bean(3, "value3"));
    }

    @Test
    public void testAsyncMultiSet() throws Throwable {
        StoreClient client = StoreClientFactory.getStoreClient();
        client.delete(new StoreKey(CATEGORY, "key3"));
        List<StoreKey> keys = new ArrayList<StoreKey>(5);
        keys.add(new StoreKey(CATEGORY, "key1"));
        keys.add(new StoreKey(CATEGORY, "key2"));
        keys.add(new StoreKey(CATEGORY, "key3"));
        keys.add(new StoreKey(CATEGORY, "key4"));
        keys.add(new StoreKey(CATEGORY, "key5"));
        List<Bean> values = new ArrayList<Bean>(5);
        values.add(new Bean(1, "value1"));
        values.add(new Bean(2, "value2"));
        values.add(new Bean(3, "value3"));
        values.add(new Bean(4, "value4"));
        values.add(new Bean(5, "value5"));
        
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        client.asyncMultiSet(keys, values, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.setResult(result);
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.setException(e);
                latch.countDown();
            }
            
        });
        latch.await(600, TimeUnit.SECONDS);
        assertEquals(Boolean.TRUE, holder.get());
        Object result = client.get(new StoreKey(CATEGORY, "key3"));
        assertEquals(result, new Bean(3, "value3"));
    }
    
    @Test
    public void testMultiCategory() {
        StoreClient client = StoreClientFactory.getStoreClient();
        StoreKey key1 = new StoreKey("testRedis", "multi");
        StoreKey key2 = new StoreKey("myredis", "multi");
        client.set(key1, 0L);
        client.set(key2, 0L);
        Object result = client.increase(key1, 1);
        assertEquals(result, 1L);
        result = client.increase(key2, 100);
        assertEquals(result, 100L);
        result = client.increase(key1, 10);
        assertEquals(result, 11L);
        result = client.increase(key2, 10);
        assertEquals(result, 110L);
    }

}
