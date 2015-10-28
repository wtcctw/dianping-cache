package com.dianping.squirrel.client.core;

public interface StoreCallback<T> {

    public void onSuccess(T result);

    public void onFailure(String msg, Throwable e);
    
}
