package com.dianping.cache.test;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import org.junit.Test;
/**
 * Created by dp on 15/12/8.
 */
public class TestClient {
    @Test
    public void test(){
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        StoreKey sk = new StoreKey("oReviewAllRankNew",195234455);
        storeClient.set(sk,1);
        storeClient.get(sk);
    }



}
