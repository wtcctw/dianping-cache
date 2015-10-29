package com.dianping.cache.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.exception.CacheException;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.squirrel.client.impl.memcached.CASResponse;
import com.dianping.squirrel.client.impl.memcached.CASValue;

@Service(url = "com.dianping.cache.test.EhcacheDemoService")
public class EhcacheDemoServiceImpl implements CacheDemoService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void ayncSetKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		try {
			cacheService.asyncSet(cacheKey, value);
		} catch (CacheException e) {
			e.printStackTrace();
		}
	}

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.set(cacheKey, value);
	}

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey(category, key.split("|"));
		return cacheService.set(cacheKey, value);
	}

	public String getKeyValue(String key) {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("myehcache", key);
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
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.remove(cacheKey);
	}

	@Override
	public long inc(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.increment(cacheKey, amount);
	}

	@Override
	public long dec(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.decrement(cacheKey, amount);
	}

	@Override
	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	@Override
	public long inc(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.increment(cacheKey, amount, def);
	}

	@Override
	public long dec(String key, int amount, long def) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.decrement(cacheKey, amount, def);
	}

	@Override
	public CASResponse cas(String key, long casId, Object value) throws CacheException, TimeoutException {
		return null;
	}

	@Override
	public CASValue gets(String key) throws CacheException, TimeoutException {
		return null;
	}

	@Override
	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException {
		return false;
	}

	@Override
	public Double getKeyDoubleValue(String key) {
		return null;
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

	private static String[] CATEGORIES = { "TGDealGroupMain", "TGDealGroupMainDetail", "TGDealGroupBase", "rs-web",
			"oStaticFileMD5", "CortexDependency", "CortexCombo", "DianPing.Common.StaticFile", "oHeaderTemplate",
			"TGMovieDiscount" };

	@Override
	public void notifyTest() {
		for (int i = 0; i < CATEGORIES.length; i++) {
			CacheKey cacheKey = new CacheKey(CATEGORIES[i], "key1", "key2", "key3");
			cacheService.get(cacheKey);
		}
	}

	@Override
	public void mAdd() {
		// TODO Auto-generated method stub
		
	}
}
