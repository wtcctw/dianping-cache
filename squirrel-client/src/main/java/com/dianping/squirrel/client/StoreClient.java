package com.dianping.squirrel.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.common.exception.StoreException;

public interface StoreClient {
    
    // sync single key operations
    public <T> T get(StoreKey key) throws StoreException;
    
    public Boolean set(StoreKey key, Object value) throws StoreException;
    
    public Boolean add(StoreKey key, Object value) throws StoreException;
    
    public Boolean delete(StoreKey key) throws StoreException;
    
    // future single key operations
    public <T> Future<T> asyncGet(StoreKey key) throws StoreException;
    
    public Future<Boolean> asyncSet(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncAdd(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncDelete(StoreKey key) throws StoreException;
    
    // callback single key operations
    public <T> Void asyncGet(StoreKey key, StoreCallback<T> callback);
    
    public Void asyncSet(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public Void asyncAdd(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public Void asyncDelete(StoreKey key, StoreCallback<Boolean> callback);
    
    // increment & decrement
    public Long increase(StoreKey key, int amount) throws StoreException;

    public Long decrease(StoreKey key, int amount) throws StoreException;
    
    // batch operations
    public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys) throws StoreException;
    
    <T> Void asyncMultiGet(List<StoreKey> keys, StoreCallback<Map<StoreKey, T>> callback);
    
    public <T> Boolean multiSet(List<StoreKey> keys, List<T> values) throws StoreException;

	<T> Void asyncMultiSet(List<StoreKey> keys, List<T> values, StoreCallback<Boolean> callback);

    // final key operations
	public String getFinalKey(StoreKey storeKey);
	
    public Boolean delete(String finalKey) throws StoreException;
    
    public boolean isDistributed();

}
