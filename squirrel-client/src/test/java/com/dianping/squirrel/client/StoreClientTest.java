package com.dianping.squirrel.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class StoreClientTest {

    @Test
    public void testCRUD() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey("myredis", "string");
        storeClient.delete(key);
        Object value = storeClient.set(key, "v1");
        assertEquals(value, Boolean.TRUE);
        value = storeClient.get(key);
        assertEquals(value, "v1");
        value = storeClient.add(key, "v2");
        assertEquals(value, Boolean.FALSE);
        value = storeClient.delete(key);
        assertEquals(value, Boolean.TRUE);
        value = storeClient.get(key);
        assertNull(value);
        value = storeClient.add(key, "v2");
        assertEquals(value, Boolean.TRUE);
    }

    @Test
    public void testIncrDecr() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        StoreKey key = new StoreKey("myredis", "integer");
        storeClient.delete(key);
        Object value = storeClient.increase(key, 1000);
        assertEquals(value, 1000L);
        value = storeClient.increase(key, 1000);
        assertEquals(value, 2000L);
        value = storeClient.decrease(key, 2000);
        assertEquals(value, 0L);
        value = storeClient.get(key);
        assertEquals(value, 0);
    }
    
    @Test
    public void testGet() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        Object value = storeClient.get(new StoreKey("myredis", "abc"));
        assertEquals(value, "abc");
    }

    @Test
    public void testSet() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        Object value = storeClient.set(new StoreKey("myredis", "key"), "abc");
        assertEquals(value, Boolean.TRUE);
    }

    @Test
    public void testAdd() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeleteStoreKey() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncGetStoreKey() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncSetStoreKeyObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncAddStoreKeyObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncDeleteStoreKey() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncGetStoreKeyStoreCallbackOfT() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncSetStoreKeyObjectStoreCallbackOfBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncAddStoreKeyObjectStoreCallbackOfBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncDeleteStoreKeyStoreCallbackOfBoolean() {
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
    public void testMultiGet() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncMultiGet() {
        fail("Not yet implemented");
    }

    @Test
    public void testMultiSet() {
        fail("Not yet implemented");
    }

    @Test
    public void testAsyncMultiSet() {
        fail("Not yet implemented");
    }

}
