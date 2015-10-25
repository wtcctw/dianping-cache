package com.dianping.cache.service.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.exception.CacheException;
import com.dianping.cache.service.CacheTestService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.squirrel.common.util.PathUtils;

@Service(url = "com.dianping.cache.service.CacheTestService")
public class CacheTestServiceImpl implements CacheTestService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Override
	public Object getKeyValue(String category, String key) {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.get(cacheKey);
	}

	@Override
	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.set(cacheKey, value);
	}

	@Override
	public void asyncSetKeyValue(String category, String key, String value) throws CacheException {
		CacheKey cacheKey = new CacheKey(category, key);
		cacheService.asyncSet(cacheKey, value);
	}

	public boolean asyncDeleteKey(String category, String key) throws CacheException, InterruptedException,
			ExecutionException {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.asyncDelete(cacheKey).get();
	}

	@Override
	public Object getKeyValue(String finalKey) {
		String category = finalKey.substring(0, finalKey.indexOf("."));
		CacheKey cacheKey = new CacheKey(category, null);
		return cacheService.get(cacheKey, finalKey);
	}

	@Override
	public void asyncSetKeyIntValue(String category, String key, int value) throws CacheException {
		CacheKey cacheKey = new CacheKey(category, key);
		cacheService.asyncSet(cacheKey, value);		
	}

    @Override
    public String getCategoryPath(String category) {
        return PathUtils.getCategoryPath(category);
    }
    
}
