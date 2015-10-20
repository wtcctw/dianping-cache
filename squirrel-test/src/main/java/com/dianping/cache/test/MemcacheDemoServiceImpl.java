package com.dianping.cache.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.avatar.cache.CacheServiceFactory;
import com.dianping.avatar.tracker.ExecutionContextHolder;
import com.dianping.avatar.tracker.TrackerContext;
import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.exception.CacheException;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import com.dianping.remote.cache.CacheManageWebService;

@Service(url = "com.dianping.cache.test.MemcacheDemoService")
public class MemcacheDemoServiceImpl implements CacheDemoService {

	private static final Logger logger = LoggerFactory.getLogger(MemcacheDemoServiceImpl.class);

	private CacheService cacheService = CacheServiceFactory.getCacheService();

	@Resource
	private CacheManageWebService cacheManageWebService;
	
	@Autowired
	private Biz biz;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public void ayncSetKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		try {
			cacheService.asyncSet(cacheKey, value);
		} catch (CacheException e) {
			logger.error("", e);
		}
	}

	public boolean setKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.set(cacheKey, value);
	}

	public boolean setKeyValue(String category, String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey(category, key.split("\\|"));
		return cacheService.set(cacheKey, value);
	}

	public String getKeyValue(String key) {
		return biz.load(key);
	}

	public Object getKeyValue(String category, String key) {
		TrackerContext ctxt = new TrackerContext();
		ctxt.setTrackRequired(true);
		ExecutionContextHolder.setTrackerContext(ctxt);

		CacheKey cacheKey = new CacheKey(category, key.split("\\|"));
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("mymemcache", key);
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
					String[] params = key.split("\\|");
					CacheKey cacheKey = new CacheKey(category, params);
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
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.remove(cacheKey);
	}

	@Override
	public long inc(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.increment(cacheKey, amount);
	}

	@Override
	public long dec(String key, int amount) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.decrement(cacheKey, amount);
	}

	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
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
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.cas(cacheKey, casId, value);
	}

	@Override
	public CASValue gets(String key) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.gets(cacheKey);
	}

	@Override
	public boolean setKeyDoubleValue(String key, Double value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.set(cacheKey, value);
	}

	@Override
	public Double getKeyDoubleValue(String key) {
		CacheKey cacheKey = new CacheKey("mymemcache", key);
		return cacheService.get(cacheKey);
	}

	private static AtomicInteger threadSeq = new AtomicInteger(0);

	@Override
	public void getHotKeyValue(final int threadNum, final int loadTime) throws CacheException, TimeoutException {
		final CacheKey cacheKey = new CacheKey("mymemcache", "hotkey");
		logger.info(">>>>>>getHotKeyValue");
		cacheService.set(cacheKey, "oldValue");

		ExecutorService executor = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			executor.execute(new Runnable() {

				long start = System.currentTimeMillis();
				int threadId = threadSeq.incrementAndGet();

				@Override
				public void run() {
					while (!Thread.interrupted()) {
						if (System.currentTimeMillis() - start > 60000) {
							break;
						}

						String value;
						try {
							value = cacheService.getOrTimeout(cacheKey);
						} catch (TimeoutException e1) {
							value = "timeout";
						}
						logger.info(">>>>>>t{} get {}", threadId, value);

						if (value == null) {
							logger.info(">>>>>>!!!loading value ...");
							try {
								Thread.sleep(loadTime);
								cacheService.set(cacheKey, "newValue");
							} catch (InterruptedException e) {
								break;
							} catch (Exception e) {
								logger.error("", e);
							}
						} else {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								break;
							}
						}
					}
				}

			});
		}

		executor.shutdown();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		cacheManageWebService.clearByCategory("mymemcache");
	}

	@Override
	public void notifyTest() {
	}

	@Override
	public void mAdd() {
		CacheKey k = new CacheKey("oShopNewReviewSummary", "a");
		User u1 = new User("w");
		User u2 = new User("x");
		List<User> list = new ArrayList<User>();
		list.add(u1);
		list.add(u2);
		cacheService.mAdd(k, list);
	}
	
	public void testAsyncCallbackGet() throws IOException, Exception {
	    CacheService cacheService = CacheServiceFactory.getCacheService();
	    CacheKey cacheKey = new CacheKey("mymemcache", "hello");
	    cacheService.set(cacheKey, "bbbbbb");
	    cacheService.asyncGet(cacheKey, new CacheCallback<String>() {

            @Override
            public void onSuccess(String result) {
                System.out.println("callback success: " + result);
            }

            @Override
            public void onFailure(String msg, Throwable e) {
                System.out.println("callback failure: " + msg);
            }
	        
	    });
	    List<CacheKey> keys = new ArrayList<CacheKey>();
	    keys.add(cacheKey);
	    cacheService.asyncBatchGet(keys, new CacheCallback<Map<CacheKey, String>>() {

            @Override
            public void onSuccess(Map<CacheKey, String> result) {
                System.out.println("callback success: " + result);
            }

            @Override
            public void onFailure(String msg, Throwable e) {
                System.out.println("callback failure: " + msg);
            }
	    });
	    System.in.read();
	}
}
