package com.dianping.cache.service.impl;

import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;

import com.dianping.cache.service.CacheTestService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.util.PathUtils;

@Service(url = "com.dianping.cache.service.CacheTestService")
public class CacheTestServiceImpl implements CacheTestService {

	@Resource
	private StoreClient storeClient;

	public void setCacheService(StoreClient StoreClient) {
		this.storeClient = StoreClient;
	}

	@Override
	public Object getKeyValue(String category, String key) {
		StoreKey cacheKey = new StoreKey(category, key);
		return storeClient.get(cacheKey);
	}

	@Override
	public boolean setKeyValue(String category, String key, String value) {
		StoreKey cacheKey = new StoreKey(category, key);
		return storeClient.set(cacheKey, value);
	}

	@Override
	public void asyncSetKeyValue(String category, String key, String value) {
		StoreKey cacheKey = new StoreKey(category, key);
		storeClient.asyncSet(cacheKey, value);
	}

	public boolean asyncDeleteKey(String category, String key) throws StoreException, InterruptedException, ExecutionException {
		StoreKey cacheKey = new StoreKey(category, key);
		return storeClient.asyncDelete(cacheKey).get();
	}

	@Override
	public Object getKeyValue(String finalKey) {
		return storeClient.get(finalKey);
	}

	@Override
	public void asyncSetKeyIntValue(String category, String key, int value) {
		StoreKey cacheKey = new StoreKey(category, key);
		storeClient.asyncSet(cacheKey, value);		
	}

    @Override
    public String getCategoryPath(String category) {
        return PathUtils.getCategoryPath(category);
    }
    
}
