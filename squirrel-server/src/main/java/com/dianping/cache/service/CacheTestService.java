package com.dianping.cache.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.exception.CacheException;

public interface CacheTestService {

	public Object getKeyValue(String finalKey);
	
	public Object getKeyValue(String category, String key);

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException;

	public void asyncSetKeyValue(String category, String key, String value) throws CacheException;

	public void asyncSetKeyIntValue(String category, String key, int value) throws CacheException;

	public boolean asyncDeleteKey(String category, String key) throws CacheException, InterruptedException,
			ExecutionException;
	
	public String getCategoryPath(String category);
	
}
