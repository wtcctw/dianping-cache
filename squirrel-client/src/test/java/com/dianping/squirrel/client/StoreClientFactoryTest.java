package com.dianping.squirrel.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class StoreClientFactoryTest {

    @Test
    public void testGetStoreClient() {
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        assertNotNull(storeClient);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetStoreClientString() {
		StoreClient storeClient = StoreClientFactory.getStoreClient("redis-hua");
        assertNotNull(storeClient);
    }

    @Test
    public void testGetStoreClientByCategory() {
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory("myredis");
        assertNotNull(storeClient);
    }

}
