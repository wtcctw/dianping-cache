package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.util.SpringLocator;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.service.AuthService;
import com.dianping.squirrel.service.impl.AuthServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class AuthTest {
    @Test
    public void testJedisAuth(){
        String password = "1234";
        String newPassword = "12345";
        Jedis jedis = new Jedis("127.0.0.1",7000);
        //jedis.auth(password);
        jedis.configSet("requirepass",newPassword);
        jedis.set("b","12345");
        jedis.configSet("masterauth",newPassword);
        System.out.println(jedis.get("b"));
    }

    @Test
    public void testJedisAuthNull(){
        String password = "12345";
        Jedis jedis = new Jedis("127.0.0.1",7000);
        jedis.auth(password);
        jedis.configSet("requirepass","");
        System.out.println(jedis.get("b"));
    }


    @Test
    public void testAuth() throws Exception {
        AuthService authService = SpringLocator.getBean(AuthServiceImpl.class);
        authService.setPassword("redis-wh","");
    }

    @Test
    public void temp(){
        StoreClient storeClient = StoreClientFactory.getStoreClient();
        //storeClient.set(new StoreKey("EmidasLotteryIpStat","vivi","vivi"),1L);
        storeClient.increase(new StoreKey("accessToken","vivi"),345);
        storeClient.get(new StoreKey("accessToken","vivi"));
        System.out.println(storeClient.get(new StoreKey("accessToken","vivi")));
    }
}
