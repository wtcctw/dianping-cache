package com.dianping.squirrel.client;

import java.util.concurrent.Future;

public interface StoreClient {

    // sync single key operations
    public <T> T get(StoreKey key) throws StoreException;
    
    public boolean set(StoreKey key, Object value) throws StoreException;
    
    public boolean add(StoreKey key, Object value) throws StoreException;
    
    public boolean delete(StoreKey key) throws StoreException;
    
    // future single key operations
    public <T> Future<T> asyncGet(StoreKey key) throws StoreException;
    
    public Future<Boolean> asyncSet(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncAdd(StoreKey key, Object value) throws StoreException;
    
    public Future<Boolean> asyncDelete(StoreKey key) throws StoreException;
    
    // callback single key operations
    public <T> void asyncGet(StoreKey key, StoreCallback<T> callback);
    
    public void asyncSet(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public void asyncAdd(StoreKey key, Object value, StoreCallback<Boolean> callback);
    
    public void asyncDelete(StoreKey key, StoreCallback<Boolean> callback);
    
}
