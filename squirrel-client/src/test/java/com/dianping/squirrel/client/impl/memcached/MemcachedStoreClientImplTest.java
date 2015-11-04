package com.dianping.squirrel.client.impl.memcached;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.impl.Bean;
import com.dianping.squirrel.client.impl.ResultHolder;

public class MemcachedStoreClientImplTest {

    private static final String CATEGORY = "mymemcache";
    
    private static final String STORE_TYPE = "memcached-wuxiang";
    
    private static final String VALUE = "dp@123456";
    
    private static final Bean BEAN = new Bean(12345678, "BEAN@12345678");
    
    @Test
    public void testGets() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        client.set(key, VALUE);
        CASValue<String> casValue = client.gets(key);
        assertEquals(VALUE, casValue.getValue());
        System.out.println(casValue);
    }

    @Test
    public void testCas() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        client.set(key, VALUE);
        CASValue<String> casValue = client.gets(key);
        assertEquals(VALUE, casValue.getValue());
        System.out.println(casValue);
        CASResponse result = client.cas(key, casValue.getCas()-1, "cas-value");
        assertEquals(CASResponse.EXISTS, result);
        result = client.cas(key, casValue.getCas(), "cas-value2");
        assertEquals(CASResponse.OK, result);
        String value = client.get(key);
        assertEquals("cas-value2", value);
    }

    @Test
    public void testGet() {
    }

    @Test
    public void testSet() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.add(key, VALUE);
        assertEquals(Boolean.TRUE, result);
        result = client.add(key, VALUE);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testDeleteFinalKey() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        String finalKey = client.getFinalKey(key);
        Object result = client.set(key, VALUE);
        assertEquals(Boolean.TRUE, result);
        result = client.delete(finalKey);
        assertEquals(Boolean.TRUE, result);
        result = client.get(key);
        assertNull(result);
    }

    @Test
    public void testAsyncGetStoreKey() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.set(key, BEAN);
        Future<Boolean> future = client.asyncDelete(key);
        assertNotNull(future);
        Boolean result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, result);
        future = client.asyncDelete(key);
        assertNotNull(future);
        result = future.get(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testAsyncGetStoreKeyStoreCallbackOfT() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        Object result = client.set(key, BEAN);
        assertEquals(Boolean.TRUE, result);
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Bean> holder = new ResultHolder<Bean>();
        client.asyncGet(key, new StoreCallback<Bean>() {

            @Override
            public void onSuccess(Bean result) {
                holder.result = result;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.exception = e;
                latch.countDown();
            }
            
        });
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(BEAN, holder.result);
    }

    @Test
    public void testAsyncSetStoreKeyObjectStoreCallbackOfBoolean() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Bean bean = new Bean(53559777, "dianping");
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncSet(key, bean, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.result = result;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.exception = e;
                latch.countDown();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.result);
        Bean bean2 = client.get(key);
        assertEquals(bean, bean2);
    }

    @Test
    public void testAsyncAddStoreKeyObjectStoreCallbackOfBoolean() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Bean bean = new Bean(53559777, "dianping");
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncAdd(key, bean, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.result = result;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.exception = e;
                latch.countDown();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.result);
        result = client.add(key, BEAN);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testAsyncDeleteStoreKeyStoreCallbackOfBoolean() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        Bean bean = new Bean(53559777, "dianping");
        client.set(key, bean);
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<Boolean> holder = new ResultHolder<Boolean>();
        Object result = client.asyncDelete(key, new StoreCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                holder.result = result;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.exception = e;
                latch.countDown();
                e.printStackTrace();
            }
            
        });
        assertNull(result);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(Boolean.TRUE, holder.result);
        result = client.delete(key);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testIncrease() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.delete(key);
        Object result = client.increase(key, 100);
        assertEquals(100L, result);
        result = client.increase(key, 100);
        assertEquals(200L, result);
    }

    @Test
    public void testDecrease() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
        StoreKey key = new StoreKey(CATEGORY, "test");
        client.set(key, 1000);
        Object result = client.decrease(key, 100);
        assertEquals(900L, result);
        result = client.decrease(key, 100);
        assertEquals(800L, result);
    }

    @Test
    public void testMultiGet() {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
    public void testAsyncMultiGet() throws Exception {
        MemcachedStoreClient client = (MemcachedStoreClient) StoreClientFactory.getStoreClient(STORE_TYPE);
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
                holder.result = result;
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable e) {
                holder.exception = e;
                latch.countDown();
            }
            
        });
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals(4, ((Map)holder.result).size());
        assertNull(((Map)holder.result).get(key6));
        assertEquals(new Bean(4, "value4"), ((Map)holder.result).get(key4));
    }

    @Test
    public void testMultiSet() {
    }

    @Test
    public void testAsyncMultiSet() {
    }

}
