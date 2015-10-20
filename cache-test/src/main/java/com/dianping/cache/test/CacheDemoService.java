package com.dianping.cache.test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.exception.CacheException;

public interface CacheDemoService {

	public void ayncSetKeyValue(String key, String value);

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException;

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException;
	
	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException;

	public Double getKeyDoubleValue(String key);

	public String getKeyValue(String key);

	public List<Object> bulkGetKeyValue(String keys);
	
	public List<Object> bulkGetKeyValue(String category, String keys);

	public boolean removeKey(String key);

	public long inc(String key, int amount) throws CacheException, TimeoutException;

	public long dec(String key, int amount) throws CacheException, TimeoutException;

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException;

	public long inc(String key, int amount, long def) throws CacheException, TimeoutException;

	public long dec(String key, int amount, long def) throws CacheException, TimeoutException;

	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException;

	public CASValue gets(String key) throws CacheException, TimeoutException;
	
	public void getHotKeyValue(int threadNum, int loadTime) throws CacheException, TimeoutException;

	public Object getKeyValue(String category, String key);
	
	public void notifyTest();
	
	public void mAdd();
}
