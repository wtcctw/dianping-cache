package com.dianping.squirrel.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.remote.cache.CacheConfigurationWebService;
import com.dianping.remote.cache.CacheManageWebService;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.GenericCacheConfigurationDTO;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.impl.ResultHolder;

public class StoreCategoryConfigManagerTest {
    
    private CacheConfigurationWebService storeConfigService;
    
    private CacheManageWebService storeManageService;
    
    public StoreCategoryConfigManagerTest() {
        storeConfigService = ServiceFactory.getService(
                "http://service.dianping.com/cacheService/cacheConfigService_1.0.0",
                CacheConfigurationWebService.class, 10000);
        
        InvokerConfig config = new InvokerConfig("http://service.dianping.com/cacheService/cacheManageService_1.0.0", CacheManageWebService.class);
        config.setCallType("oneway");
        config.setTimeout(10000);
        this.storeManageService = ((CacheManageWebService)ServiceFactory.getService(config));
    }
    
    @Test
    public void testGetCacheKeyType() {
        StoreCategoryConfig categoryConfig = StoreCategoryConfigManager.getInstance().findCacheKeyType("non-exist");
        assertNotNull(categoryConfig);
        assertTrue(categoryConfig instanceof DefaultStoreCategoryConfig);
        System.out.println(categoryConfig);
        categoryConfig = StoreCategoryConfigManager.getInstance().findCacheKeyType("mymemcache");
        assertNotNull(categoryConfig);
        assertFalse(categoryConfig instanceof DefaultStoreCategoryConfig);
        System.out.println(categoryConfig);
    }
    
    @Test
    public void testCategoryConfigListener() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        final ResultHolder<StoreCategoryConfig> holder = new ResultHolder<StoreCategoryConfig>();
        
        StoreCategoryConfigManager.getInstance().addConfigListener(new StoreCategoryConfigListener() {
            
            @Override
            public void configRemoved(StoreCategoryConfig categoryConfig) {
                holder.setResult(categoryConfig);
                latch.countDown();
            }
            
            @Override
            public void configChanged(StoreCategoryConfig categoryConfig) {
                holder.setResult(categoryConfig);
                latch.countDown();
            }
        });
        
        StoreCategoryConfig categoryConfig = StoreCategoryConfigManager.getInstance().findCacheKeyType("mymemcache");
        CacheKeyConfigurationDTO configDto = storeConfigService.getKeyConfiguration("mymemcache");
        configDto.setIndexDesc("hahaha");
        configDto.setAddTime(System.currentTimeMillis());
        storeManageService.updateCacheKeyConfig(configDto);
        
        try {
            latch.await(100, TimeUnit.SECONDS);
            assertNotNull(holder.get());
            System.out.println(holder.get());
            assertEquals("hahaha", holder.get().getIndexDesc());
        } catch (Exception e) {
            assertTrue(false);
        }
        
    }

}
