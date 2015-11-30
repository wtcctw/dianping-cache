package com.dianping.cache.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.exception.CacheException;

public interface CacheTestService {

	Object getKeyValue(String finalKey);
	
	Object getKeyValue(String category, String key);

	boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException;

	void asyncSetKeyValue(String category, String key, String value) throws CacheException;

	void asyncSetKeyIntValue(String category, String key, int value) throws CacheException;

	boolean asyncDeleteKey(String category, String key) throws CacheException, InterruptedException,
			ExecutionException;
	
	String getCategoryPath(String category);
	
}
