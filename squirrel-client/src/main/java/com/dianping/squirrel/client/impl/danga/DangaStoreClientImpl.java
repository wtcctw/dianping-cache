package com.dianping.squirrel.client.impl.danga;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danga.MemCached.MemCachedClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.StoreCategoryConfig;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreFuture;
import com.dianping.squirrel.client.impl.AbstractStoreClient;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.util.CacheKeyUtils;

/**
 * Created by dp on 15/11/30.
 */
public class DangaStoreClientImpl extends AbstractStoreClient implements DangaStoreClient{

	private static Logger logger = LoggerFactory.getLogger(DangaStoreClientImpl.class);
	
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	
	private static int hotKeyHitRange = configManager.getIntValue("avatar-cache.memcached.hotkey.hitrange", 10000);
	
	private DangaClientConfig config;
	
	private volatile DangaClientManager clientManager;


	@Override
	public String getScheme() {
		return "danga";
	}

	@Override
	public void configure(StoreClientConfig config) {
		this.config = (DangaClientConfig)config;
		//NodeMonitor.getInstance().clear(storeType);
	}

	@Override
	public void start() {
		clientManager = new DangaClientManager(config);
		clientManager.start();
	}

	@Override
	public void stop() {
		if(clientManager != null)
			clientManager.stop();
	}

	@Override
	public void configChanged(StoreClientConfig config0) {
		logger.info("memcached store client config changed: " + config0);
		this.config = (DangaClientConfig) config0;
		DangaClientManager oldClientManager = this.clientManager;
		DangaClientManager newClientManager = new DangaClientManager(config);
		newClientManager.start();
		this.clientManager = newClientManager;
		oldClientManager.stop();
	}

	@Override
	protected <T> T doGet(StoreCategoryConfig categoryConfig, String key)
			throws Exception {
		boolean isHot = categoryConfig.isHot();
        String category = categoryConfig.getCategory();
        TimeoutException timeoutException = null;
        Object result = null;
        String finalKey = CacheKeyUtils.nextCacheKey(key, isHot, hotKeyHitRange);
        try {
            result = doGet(finalKey, category);
        } catch (TimeoutException e) {
            logger.error("memcached get {} timeout", key);
            timeoutException = e;
            result = null;
        }
        if (isHot) {
            if (result == null && timeoutException == null) {
                if (finalKey.endsWith(CacheKeyUtils.SUFFIX_HOT)) {
                    try {
                        result = doGet(getCacheKey(key, isHot), category);
                    } catch (TimeoutException e) {
                        timeoutException = e;
                        result = null;
                    }
                }
            }
        }
        if (timeoutException != null && result == null) {
            throw timeoutException;
        }
        return (T) result;
	}
	
	private Object doGet(String key, String category) throws Exception {
		Object result;
		MemCachedClient client = getReadClient();
		result = client.get(key);
		return result;
	}
	
	
	
	@Override
	protected Boolean doSet(StoreCategoryConfig categoryConfig,
			String finalKey, Object value) throws Exception {
		boolean isHot = categoryConfig.isHot();
        int expiration = categoryConfig.getDurationSeconds();
        String k = getCacheKey(finalKey, isHot);
        Object v = getCacheValue(value, expiration, isHot);
        MemCachedClient client = getWriteClient();
		return client.set(k, v, new Date(expiration*1000));
	}

	@Override
	protected Boolean doAdd(StoreCategoryConfig categoryConfig,
			String finalKey, Object value) throws Exception {
		boolean isHot = categoryConfig.isHot();
        int expiration = categoryConfig.getDurationSeconds();
		String k = getCacheKey(finalKey, isHot);
		Object v = getCacheValue(value, expiration, isHot);
		return getWriteClient().add(k, v, new Date(expiration*1000));
	}

	@Override
	protected Boolean doDelete(StoreCategoryConfig categoryConfig,
			String finalKey) throws Exception {
		return getWriteClient().delete(finalKey);
	}

	@Override
	protected <T> Future<T> doAsyncGet(final StoreCategoryConfig categoryConfig,
									   final String key) throws Exception {
		return new DangaAsyncCommand<T>(){
			@Override
			public T excute() throws Throwable {
				return doGet(categoryConfig,key);
			}
		}.run(key);
	}

	@Override
	protected Future<Boolean> doAsyncSet(final StoreCategoryConfig categoryConfig,
			final String finalKey, final Object value) throws Exception {
		return new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doSet(categoryConfig,finalKey,value);
			}
		}.run(finalKey);
	}

	@Override
	protected Future<Boolean> doAsyncAdd(final StoreCategoryConfig categoryConfig,
			final String finalKey,final Object value) throws Exception {
		return new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doAdd(categoryConfig,finalKey,value);
			}
		}.run(finalKey);
	}

	@Override
	protected Future<Boolean> doAsyncDelete(final StoreCategoryConfig categoryConfig,
											final String finalKey) throws Exception {
		return new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doDelete(categoryConfig,finalKey);
			}
		}.run(finalKey);
	}

	@Override
	protected <T> Void doAsyncGet(final StoreCategoryConfig categoryConfig,
								  final String finalKey,StoreCallback<T> callback) throws Exception {
		new DangaAsyncCommand<T>(){
			@Override
			public T excute() throws Throwable {
				return doGet(categoryConfig,finalKey);
			}
		}.run(callback);
		return null;
	}

	@Override
	protected Void doAsyncSet(final StoreCategoryConfig categoryConfig,
							  final String finalKey, final Object value, StoreCallback<Boolean> callback)
			throws Exception {
		new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doSet(categoryConfig,finalKey,value);
			}
		}.run(callback);
		return null;
	}

	@Override
	protected Void doAsyncAdd(final StoreCategoryConfig categoryConfig,
							  final String finalKey, final Object value, StoreCallback<Boolean> callback)
			throws Exception {
		new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doAdd(categoryConfig,finalKey,value);
			}
		}.run(callback);
		return null;
	}

	@Override
	protected Void doAsyncDelete(final StoreCategoryConfig categoryConfig,
								 final String finalKey, StoreCallback<Boolean> callback) throws Exception {
		new DangaAsyncCommand<Boolean>(){
			@Override
			public Boolean excute() throws Throwable {
				return doDelete(categoryConfig,finalKey);
			}
		}.run(callback);
		return null;
	}

	@Override
	protected Long doIncrease(StoreCategoryConfig categoryConfig,
			String finalKey, int amount) throws Exception {
		
		long ret = getWriteClient().incr(finalKey, amount);
		if(ret == -1){ //counter does not exits
			getWriteClient().set(finalKey, amount);
			return (long)amount;
		}
		return ret;
	}

	@Override
	protected Long doDecrease(StoreCategoryConfig categoryConfig,
			String finalKey, int amount) throws Exception {
		long ret = getWriteClient().decr(finalKey, amount);
		if(ret == -1){ //counter does not exits
			getWriteClient().set(finalKey, (long)(0-amount));
			return (long)(0-amount);
		}
		return ret;
	}

	@Override
	protected <T> Map<String, T> doMultiGet(StoreCategoryConfig categoryConfig,
			List<String> finalKeyList) throws Exception {
		String[] keyArray = new String[finalKeyList.size()];
		finalKeyList.toArray(keyArray);
		return (Map<String, T>) getReadClient().getMulti(keyArray);
	}

	@Override
	protected <T> Void doAsyncMultiGet(final StoreCategoryConfig categoryConfig,
									   final List<String> finalKeyList, StoreCallback<Map<String, T>> callback)
			throws Exception {
		new DangaAsyncCommand<Map<String, T>>(){
			@Override
			public Map<String, T> excute() throws Throwable {
				return doMultiGet(categoryConfig,finalKeyList);
			}
		}.run(callback);
		return null;
	}

	@Override
	protected <T> Boolean doMultiSet(StoreCategoryConfig categoryConfig,
			List<String> finalKeyList, List<T> values) throws Exception {
		throw new UnsupportedOperationException(
				"Danga memcached client  does not support multiSet operations");
	}

	@Override
	protected <T> Void doAsyncMultiSet(StoreCategoryConfig categoryConfig,
			List<String> keys, List<T> values, StoreCallback<Boolean> callback)
			throws Exception {
		throw new UnsupportedOperationException(
				"Danga memcached client  does not support multiSet operations");
	}

	private MemCachedClient getReadClient() {
		return clientManager.getReadClient();
	}

	private MemCachedClient getWriteClient() {
		return clientManager.getWriteClient();
	}
	
    private String getCacheKey(String key, boolean isHot) {
        return CacheKeyUtils.reformKey(key);
    }
    
    private Object getCacheValue(Object value, int expiration, boolean isHot) {
        return value;
    }

	@Override
	public <T> Boolean multiSet(List<StoreKey> keys, List<T> values)
			throws StoreException {
		throw new UnsupportedOperationException(
				"Danga memcached client  does not support multiSet operations");
	}

	@Override
	public <T> CASValue<T> gets(StoreKey key) throws StoreException {
		checkNotNull(key, "store key is null");
        final StoreCategoryConfig categoryConfig = super.configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());

        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doGets(categoryConfig, finalKey);
            }

        }, categoryConfig, finalKey, "gets");
	}

	protected <T> CASValue<T>  doGets(StoreCategoryConfig categoryConfig, String finalKey) {
		String reformedKey = CacheKeyUtils.reformKey(finalKey);
		com.schooner.MemCached.MemcachedItem value =  getReadClient().gets(reformedKey);
		CASValue<T> casValue = null;
		if(value != null){
			casValue = new CASValue<T>(value.getCasUnique(), (T)value.getValue());
		}
		return casValue;
	}

	@Override
	public boolean cas(StoreKey key, long casId, Object value)
			throws StoreException {
		checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        final StoreCategoryConfig categoryConfig = super.configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
		return getReadClient().cas(finalKey, value, casId);
	}

	public ThreadPoolExecutor getThreadPool() {
		return clientManager.getThreadPool();
	}

	class DangaAsyncCommand<T>{

		public T excute() throws Throwable{
			return null;
		}

		public  StoreFuture<T> run(String key){
			final StoreFuture<T> future = new StoreFuture<T>(key);
			getThreadPool().submit(new Runnable() {
				@Override
				public void run() {
					try {
						T t = excute();
						future.onSuccess(t);
					} catch (Throwable e){
						future.onFailure(e);
					}
				}
			});
			return future;
		}

		public void run(final StoreCallback<T> callback){
			getThreadPool().submit(new Runnable() {
				@Override
				public void run() {
					try {
						T t = excute();
						callback.onSuccess(t);
					} catch (Throwable e){
						callback.onFailure(e);
					}
				}
			});
		}
	}
}
