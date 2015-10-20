package com.dianping.cache.core;

public interface CacheCallback<T> {

	public void onSuccess(T result);

	public void onFailure(String msg, Throwable e);
}
