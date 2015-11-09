package com.dianping.squirrel.client.config;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.remote.cache.CacheConfigurationWebService;
import com.dianping.remote.cache.CacheManageWebService;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.GenericCacheConfigurationDTO;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.impl.ResultHolder;
import com.dianping.squirrel.client.impl.redis.RedisClientConfig;

public class StoreClientConfigManagerTest {
    
    private CacheConfigurationWebService storeConfigService;
    
    private CacheManageWebService storeManageService;
    
    public StoreClientConfigManagerTest() {
        storeConfigService = ServiceFactory.getService(
                "http://service.dianping.com/cacheService/cacheConfigService_1.0.0",
                CacheConfigurationWebService.class, 10000);
        
        InvokerConfig config = new InvokerConfig("http://service.dianping.com/cacheService/cacheManageService_1.0.0", CacheManageWebService.class);
        config.setCallType("oneway");
        config.setTimeout(10000);
        this.storeManageService = ((CacheManageWebService)ServiceFactory.getService(config));
    }
    
    @Test
    public void testClientConfigChange() throws Exception {
        final String cluster = "redis-hua";
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<RedisClientConfig> holder = new ResultHolder<RedisClientConfig>();
        
        StoreClientConfigManager.getInstance().addConfigListener(cluster, new StoreClientConfigListener() {
            @Override
            public void configChanged(StoreClientConfig config) {
                System.out.println(config);
                holder.result = (RedisClientConfig)config;
                latch.countDown();
            }
        });
        
        StoreClient client = StoreClientFactory.getStoreClient(cluster);
        Thread.sleep(5000);
        
        CacheConfigurationDTO configDto = storeConfigService.getCacheConfiguration(cluster);
        String originalServer = configDto.getServers();
        GenericCacheConfigurationDTO genericConfig = new GenericCacheConfigurationDTO();
        genericConfig.setCacheKey(configDto.getCacheKey());
        genericConfig.setClientClazz(configDto.getClientClazz());
        genericConfig.setServers("redis-cluster://192.168.224.71:7000,192.168.224.149:7000?readTimeout=100&connTimeout=1000&maxRedirects=1");
        genericConfig.setTranscoderClazz(configDto.getTranscoderClazz());
        storeManageService.updateConfiguration(genericConfig);
        
        try {
            latch.await(100, TimeUnit.SECONDS);
            assertNotNull(holder.result);
            assertEquals(1, holder.result.getMaxRedirects());
        } catch (Exception e) {
            assertTrue(false);
        }
        
        System.in.read();
    }

}
