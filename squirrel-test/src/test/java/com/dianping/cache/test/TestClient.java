package com.dianping.cache.test;

import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by dp on 15/12/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/appcontext-*.xml")
public class TestClient {

    @Autowired
    Biz biz;

    @Test
    public void test(){
        RedisStoreClient storeClient = (RedisStoreClient) StoreClientFactory.getStoreClientByCategory("HuiUserTicket");
        StoreKey sk = new StoreKey("mt-spu-shop",195234455);
        storeClient.set(sk,1);
        storeClient.get(sk);
    }

    @Test
    public void testS(){
        System.out.println(biz.load("si"));
    }

}
