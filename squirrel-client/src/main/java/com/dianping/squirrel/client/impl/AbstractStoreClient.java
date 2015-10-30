package com.dianping.squirrel.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.CacheKeyType;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.log.LoggerLoader;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.client.monitor.TimeMonitor;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreTimeoutException;
import com.dianping.squirrel.common.util.PathUtils;

public abstract class AbstractStoreClient implements StoreClient {

    static {
        LoggerLoader.init();
    }
    
	protected StoreCategoryConfigManager configManager;

	public AbstractStoreClient() {
		configManager = StoreCategoryConfigManager.getInstance();
	}
	
	@Override
    public Boolean delete(final String finalKey) throws StoreException {
	    checkNotNull(finalKey, "final key is null");
	    String category = PathUtils.getCategoryFromKey(finalKey);
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(category);
        checkNotNull(categoryConfig, "%s' category config is null", category);
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doDelete(categoryConfig, finalKey);
            }
            
        }, categoryConfig, finalKey, "delete");
	}
    
	@Override
	public <T> T get(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
		final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());
		
		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doGet(categoryConfig, finalKey);
			}
			
		}, categoryConfig, finalKey, "get");
	}

	protected abstract <T> T doGet(CacheKeyType categoryConfig, String finalKey) throws Exception;
	
	@Override
	public Boolean set(StoreKey key, final Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
		final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());
		
		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doSet(categoryConfig, finalKey, value);
			}
			
		}, categoryConfig, finalKey, "set");
	}

	protected abstract Boolean doSet(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception;

	@Override
	public Boolean add(StoreKey key, final Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
		final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());
		
		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAdd(categoryConfig, finalKey, value);
			}
			
		}, categoryConfig, finalKey, "add");
	}

	protected abstract Boolean doAdd(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception;
	
	@Override
	public Boolean delete(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
		final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());
		
		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doDelete(categoryConfig, finalKey);
			}
			
		}, categoryConfig, finalKey, "delete");
	}

	protected abstract Boolean doDelete(CacheKeyType categoryConfig, String finalKey) throws Exception;
	
	@Override
	public <T> Future<T> asyncGet(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncGet(categoryConfig, finalKey);
            }
            
        }, categoryConfig, finalKey, "asyncGet");
	}
	
	protected abstract <T> Future<T> doAsyncGet(CacheKeyType categoryConfig, String finalKey) throws Exception;

	@Override
	public Future<Boolean> asyncSet(StoreKey key, final Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncSet(categoryConfig, finalKey, value);
            }
            
        }, categoryConfig, finalKey, "asyncSet");
	}

	protected abstract Future<Boolean> doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception;

    @Override
	public Future<Boolean> asyncAdd(StoreKey key, final Object value) throws StoreException {
        checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncAdd(categoryConfig, finalKey, value);
            }
            
        }, categoryConfig, finalKey, "asyncAdd");
	}

	protected abstract Future<Boolean> doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value) throws Exception;

    @Override
	public Future<Boolean> asyncDelete(StoreKey key) throws StoreException {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncDelete(categoryConfig, finalKey);
            }
            
        }, categoryConfig, finalKey, "asyncDelete");
	}

	protected abstract Future<Boolean> doAsyncDelete(CacheKeyType categoryConfig, String finalKey) throws Exception;

    @Override
	public <T> Void asyncGet(StoreKey key, final StoreCallback<T> callback) {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncGet(categoryConfig, finalKey, callback);
            }
            
        }, categoryConfig, finalKey, "asyncGet");
	}

    protected abstract <T> Void doAsyncGet(CacheKeyType categoryConfig, String finalKey, StoreCallback<T> callback) throws Exception;
    
	@Override
	public Void asyncSet(StoreKey key, final Object value, final StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
	    checkNotNull(value, "value is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncSet(categoryConfig, finalKey, value, callback);
            }
            
        }, categoryConfig, finalKey, "asyncSet");
	}

	protected abstract Void doAsyncSet(CacheKeyType categoryConfig, String finalKey, Object value, StoreCallback<Boolean> callback) throws Exception;
	
	@Override
	public Void asyncAdd(StoreKey key, final Object value, final StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncAdd(categoryConfig, finalKey, value, callback);
            }
            
        }, categoryConfig, finalKey, "asyncAdd");
    }

    protected abstract Void doAsyncAdd(CacheKeyType categoryConfig, String finalKey, Object value, StoreCallback<Boolean> callback) throws Exception;

	@Override
	public Void asyncDelete(StoreKey key, final StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doAsyncDelete(categoryConfig, finalKey, callback);
            }
            
        }, categoryConfig, finalKey, "asyncDelete");
    }

    protected abstract Void doAsyncDelete(CacheKeyType categoryConfig, String finalKey, StoreCallback<Boolean> callback) throws Exception;

	@Override
	public Long increase(StoreKey key, final int amount) throws StoreException {
	    checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doIncrease(categoryConfig, finalKey, amount);
            }
            
        }, categoryConfig, finalKey, "increase");
	}

	protected abstract Long doIncrease(CacheKeyType categoryConfig, String finalKey, int amount) throws Exception;

    @Override
	public Long decrease(StoreKey key, final int amount) throws StoreException {
        checkNotNull(key, "store key is null");
        final CacheKeyType categoryConfig = configManager.findCacheKeyType(key.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
        final String finalKey = categoryConfig.getKey(key.getParams());
        
        return executeWithMonitor(new Command() {

            @Override
            public Object execute() throws Exception {
                return doDecrease(categoryConfig, finalKey, amount);
            }
            
        }, categoryConfig, finalKey, "decrease");
    }

    protected abstract Long doDecrease(CacheKeyType categoryConfig, String finalKey, int amount) throws Exception;

	@Override
	public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys)
			throws StoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Void asyncMultiGet(List<StoreKey> keys, StoreCallback<Map<StoreKey, T>> callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Boolean multiSet(List<StoreKey> keys, List<T> values)
			throws StoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> Void asyncMultiSet(List<StoreKey> keys, List<T> values, StoreCallback<Boolean> callback) {
		// TODO Auto-generated method stub
		return  null;
	}

    @Override
    public boolean isDistributed() {
        return true;
    }
    
	protected <T> T executeWithMonitor(Command command, CacheKeyType categoryConfig, String finalKey, String action) {
		String storeType = categoryConfig.getCacheType();
		String category = categoryConfig.getCategory();
		
		Transaction t = null;
		if (needMonitor(storeType)) {
			t = Cat.getProducer().newTransaction("Store." + storeType, category + ":" + action);
			t.addData("finalKey", finalKey);
			t.setStatus(Message.SUCCESS);
		}
		StatusHolder.flowIn(storeType, category, action);
		long begin = System.nanoTime();
		int second = (int) (begin / 1000000000 % 60) + 1;
		try {
		    Cat.getProducer().logEvent("Store." + storeType + ".qps", "S"+second);
			Object result = command.execute();
			TimeMonitor.getInstance().logTime(storeType, category, action, System.nanoTime() - begin);
			return (T) result;
		} catch (TimeoutException e) {
			TimeMonitor.getInstance().logTime(storeType, category, action, System.nanoTime() - begin, "timeout");
			Cat.getProducer().logEvent("Store." + storeType, category + ":timeout", Message.SUCCESS, "");
			if (t != null) {
				t.setStatus(e);
			}
			throw new StoreTimeoutException(e);
		} catch(StoreException e) {
			Cat.getProducer().logError(e);
			if(t != null) {
				t.setStatus(e);
			}
			throw e;
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
			if (t != null) {
				t.setStatus(e);
			}
			throw new StoreException(e);
		} finally {
			StatusHolder.flowOut(storeType, category, action);
			if (t != null) {
				t.complete();
			}
		}
	}

	protected boolean needMonitor(String cacheType) {
		return true;
	}
	
	public static interface Command {

	    Object execute() throws Exception;
	    
	}
	
}
