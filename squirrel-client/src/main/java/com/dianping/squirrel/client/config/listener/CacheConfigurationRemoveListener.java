package com.dianping.squirrel.client.config.listener;

import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.client.core.StoreClientBuilder;
import com.dianping.squirrel.common.domain.CacheConfigurationRemoveDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dp on 15/12/22.
 */
public class CacheConfigurationRemoveListener {
    private final Logger logger = LoggerFactory.getLogger(CacheConfigurationRemoveListener.class);

    public void handleMessage(CacheConfigurationRemoveDTO msg){
        if(msg != null){
            String cacheKey = msg.getCacheKey();
            StoreClientConfigManager.getInstance().removeCacheConfig(cacheKey);
            StoreClientBuilder.closeStoreClient(cacheKey);
            com.dianping.squirrel.client.core.CacheConfiguration.removeCache(cacheKey);
            logger.warn("Removed CacheConfiguration {} success !",cacheKey);
        }
    }
}
