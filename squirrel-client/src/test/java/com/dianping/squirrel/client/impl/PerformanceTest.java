package com.dianping.squirrel.client.impl;

import org.junit.Test;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;

public class PerformanceTest {

    private String category = "myredis";
    private StoreClient client = StoreClientFactory.getStoreClient();
    private StoreKey key = new StoreKey(category, "perf");
    private Bean bean = new Bean(1111, "1111");
    private Bean bean2 = new Bean(2222, "2222");
    
    @Test
    public void testPerformance() {
        int loop = 10000;
        for(int i=0; i<loop; i++) {
            testSingle(i);
        }
    }

    private void testSingle(int loop) {
        StoreKey key = new StoreKey(category, loop);
        try {
            client.delete(key);
        } catch(Exception e) {
            System.err.println(e);
        }
        try {
            client.add(key, bean);
        } catch(Exception e) {
            System.err.println(e);
        }
        try {
            client.set(key, bean2);
        } catch(Exception e) {
            System.err.println(e);
        }
        try {
            Object result = client.get(key);
            System.out.println(loop + ":" + result);
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    
}
