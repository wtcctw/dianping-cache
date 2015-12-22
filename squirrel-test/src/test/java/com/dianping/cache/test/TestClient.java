package com.dianping.cache.test;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;
import org.junit.Test;
/**
 * Created by dp on 15/12/8.
 */
public class TestClient {
    @Test
    public void test(){
        RedisStoreClient storeClient = (RedisStoreClient) StoreClientFactory.getStoreClientByCategory("HuiUserTicket");
        StoreKey sk = new StoreKey("HuiUserTicket",1);
        storeClient.set(sk,1);
        storeClient.get(sk);
    }
}
