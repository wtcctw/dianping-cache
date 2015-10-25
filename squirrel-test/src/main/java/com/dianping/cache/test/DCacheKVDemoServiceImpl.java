package com.dianping.cache.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.tracker.ExecutionContextHolder;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.cache.exception.CacheException;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.squirrel.client.core.CASResponse;
import com.dianping.squirrel.client.core.CASValue;

@Service(url = "com.dianping.cache.test.DCacheKVDemoService")
public class DCacheKVDemoServiceImpl implements CacheDemoService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void ayncSetKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		try {
			cacheService.asyncSet(cacheKey, value);
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.set(cacheKey, value);
	}

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey(category, key.split("\\|"));
		return cacheService.set(cacheKey, value);
	}

	public String getKeyValue(String key) {
		TrackerContext ctxt = new TrackerContext();
		ctxt.setTrackRequired(true);
		ExecutionContextHolder.setTrackerContext(ctxt);

		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("mydcache", key);
					cacheKeys.add(cacheKey);
				}
			}
		}
		if (!CollectionUtils.isEmpty(cacheKeys)) {
			return cacheService.mGet(cacheKeys, false);
		}
		return null;
	}

	@Override
	public List<Object> bulkGetKeyValue(String category, String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey(category, key);
					cacheKeys.add(cacheKey);
				}
			}
		}
		if (!CollectionUtils.isEmpty(cacheKeys)) {
			return cacheService.mGet(cacheKeys, false);
		}
		return null;
	}

	@Override
	public boolean removeKey(String key) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.remove(cacheKey);
	}

	@Override
	public long inc(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.increment(cacheKey, amount);
	}

	@Override
	public long dec(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.decrement(cacheKey, amount);
	}

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	@Override
	public long inc(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.increment(cacheKey, amount, def);
	}

	@Override
	public long dec(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.decrement(cacheKey, amount, def);
	}

	@Override
	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.cas(cacheKey, casId, value);
	}

	@Override
	public CASValue gets(String key) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.gets(cacheKey);
	}

	@Override
	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.set(cacheKey, value);
	}

	@Override
	public Double getKeyDoubleValue(String key) {
		CacheKey cacheKey = new CacheKey("mydcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public void getHotKeyValue(int threadNum, int loadTime) throws CacheException, TimeoutException {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getKeyValue(String category, String key) {
		CacheKey cacheKey = new CacheKey(category, key);
		return cacheService.get(cacheKey);
	}

	@Override
	public void notifyTest() {
	}

	@Override
	public void mAdd() {
		// TODO Auto-generated method stub

	}

}
