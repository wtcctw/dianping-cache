package com.dianping.squirrel.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class StoreClientTest {

    @Test
    public void testAll() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        Object value = storeClient.set(new StoreKey("myredis", "key"), "abc");
        assertEquals(value, Boolean.TRUE);
        value = storeClient.get(new StoreKey("myredis", "key"));
        assertEquals(value, "abc");
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
