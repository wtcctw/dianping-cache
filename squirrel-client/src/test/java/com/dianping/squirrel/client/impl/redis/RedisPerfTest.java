package com.dianping.squirrel.client.impl.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.core.StoreCallback;

public class RedisPerfTest {

    private static final String CATEGORY = "myredis";
    
    private enum Operation {Get, Set, AsyncGet, AsyncSet};
    
    @Test
    public void test() throws Throwable {
         final int keyBatch = 100;
         final int valueSize = 100;
         final int loop = 10;
         final Operation operation = Operation.AsyncGet;
         int thread = 1;
         initTest(operation, keyBatch, valueSize);
         ExecutorService executor = Executors.newFixedThreadPool(thread);
         long start = System.currentTimeMillis();
         executor.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    switch(operation) {
                    case Get:
                        perfTestGet(loop, keyBatch, valueSize);
                        break;
                    case Set:
                        perfTestSet(loop, keyBatch, valueSize);
                        break;
                    case AsyncGet:
                        perfTestAsyncGet(loop, keyBatch, valueSize);
                        break;
                    case AsyncSet:
                        perfTestAsyncSet(loop, keyBatch, valueSize);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
             
         });
         executor.shutdown();
         executor.awaitTermination(10, TimeUnit.MINUTES);
         long span = System.currentTimeMillis() - start;
         System.out.println("time: "+ span/1000 + "s, qps: " + (loop * keyBatch * 4.0 / span * 1000));
    }
    
    private void initTest(Operation operation, int keyBatch, int valueSize) {
        StoreClient client = StoreClientFactory.getStoreClientByCategory(CATEGORY);
        List<StoreKey> keys = getKeys(keyBatch);
        for(StoreKey key : keys) {
            client.set(key, RandomStringUtils.randomAlphanumeric(valueSize));
        }
    }
    
    private List<StoreKey> getKeys(int keyBatch) {
        List<StoreKey> keys = new ArrayList<StoreKey>(keyBatch);
        for(int i=0; i<keyBatch; i++) {
            keys.add(new StoreKey(CATEGORY, "perf" + i));
        }
        return keys;
    }
    
    private List<String> getValues(int keyBatch, int valueSize) {
        List<String> values = new ArrayList<String>(keyBatch);
        for(int i=0; i<keyBatch; i++) {
            values.add(RandomStringUtils.randomAlphanumeric(valueSize));
        }
        return values;
    }

    private void perfTestGet(int loop, int keyBatch, int valueSize) throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        List<StoreKey> keys = getKeys(keyBatch);
        for(int i=0; i<loop; i++) {
            Map<StoreKey, String> getResult = client.multiGet(keys);
        }
    }
    
    private void perfTestSet(int loop, int keyBatch, int valueSize) throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        List<StoreKey> keys = getKeys(keyBatch);
        for(int i=0; i<loop; i++) {
            List<String> values = getValues(keyBatch, valueSize);
            client.multiSet(keys, values);
        }
    }
    
    private void perfTestAsyncGet(int loop, int keyBatch, int valueSize) throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        List<StoreKey> keys = getKeys(keyBatch);
        List<String> values = getValues(keyBatch, valueSize);
        final CountDownLatch latch = new CountDownLatch(loop);;
        for(int i=0; i<loop; i++) {
            client.asyncMultiGet(keys, new StoreCallback<Map<StoreKey, String>>() {

                @Override
                public void onSuccess(Map<StoreKey, String> result) {
                    latch.countDown();
                }

                @Override
                public void onFailure(Throwable e) {
                    latch.countDown();
                    e.printStackTrace();
                }
                
            });
            Thread.sleep(100);
        }
        latch.await();
    }

    private void perfTestAsyncSet(int loop, int keyBatch, int valueSize) throws Exception {
        StoreClient client = StoreClientFactory.getStoreClient();
        List<StoreKey> keys = getKeys(keyBatch);
        List<String> values = getValues(keyBatch, valueSize);
        final CountDownLatch latch = new CountDownLatch(loop);;
        for(int i=0; i<loop; i++) {
            values = getValues(keyBatch, valueSize);
            client.asyncMultiSet(keys, values, new StoreCallback<Boolean>() {
                
                @Override
                public void onSuccess(Boolean result) {
                    latch.countDown();
                }
                
                @Override
                public void onFailure(Throwable e) {
                    latch.countDown();
                    e.printStackTrace();
                }
                
            });
        }
        latch.await();
    }
    
}
