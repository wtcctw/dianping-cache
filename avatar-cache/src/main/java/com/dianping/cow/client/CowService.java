package com.dianping.cow.client;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public interface CowService {

    // sync single key operations
    public <T> T get(CowKey key) throws TimeoutException, CowException;
    
    public boolean set(CowKey key, Object value) throws TimeoutException, CowException;
    
    public boolean add(CowKey key, Object value) throws TimeoutException, CowException;
    
    public boolean delete(CowKey key) throws TimeoutException, CowException;
    
    // future single key operations
    public <T> Future<T> asyncGet(CowKey key) throws CowException;
    
    public Future<Boolean> asyncSet(CowKey key, Object value) throws CowException;
    
    public Future<Boolean> asyncAdd(CowKey key, Object value) throws CowException;
    
    public Future<Boolean> asyncDelete(CowKey key) throws CowException;
    
    // callback single key operations
    public <T> void asyncGet(CowKey key, CowCallback<T> callback);
    
    public void asyncSet(CowKey key, Object value, CowCallback<Boolean> callback);
    
    public void asyncAdd(CowKey key, Object value, CowCallback<Boolean> callback);
    
    public void asyncDelete(CowKey key, CowCallback<Boolean> callback);
    
}
