package com.dianping.squirrel.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.dianping.squirrel.common.exception.StoreException;

public interface Store {
	
	// get
	<T> T get(final String key, Class dataType, final String category) throws StoreException;
	
	<T> T get(final String key, Class dataType, long timeout, final boolean isHot, final String category)
			throws StoreException;
	
	<T> Future<T> asyncGet(final String key, Class dataType, final boolean isHot, final String category)
			throws StoreException;
	
	<T> void asyncGet(final String key, Class dataType, long timeout, final boolean isHot, final String category, 
			final StoreCallback<T> callback);
	
	// set
	boolean set(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException;
	
	boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException;
	
	Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException;

	void asyncSet(String key, Object value, int expiration, long timeout, boolean isHot, String category, 
			StoreCallback<Boolean> callback);

	// add
	boolean add(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException;

	boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
			throws StoreException;

	Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
			throws StoreException;

	void asyncAdd(String key, Object value, int expiration, long timeout, boolean isHot, String category, 
			StoreCallback<Boolean> callback);
	
	// delete
	boolean delete(String key, boolean isHot, String category) throws StoreException;
	
	boolean delete(String key, long timeout, boolean isHot, String category) throws StoreException;
	
	Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws StoreException;
	
	void asyncDelete(String key, long timeout, boolean isHot, String category, StoreCallback<Boolean> callback);

	// batch get
	<T> Map<String, T> batchGet(List<String> keys, Class dataType, boolean isHot, String category)
			throws StoreException;
	
	<T> void asyncBatchGet(final List<String> keys, Class dataType, final boolean isHot, final String category,
			final StoreCallback<Map<String, T>> callback);
	
	// batch set
	<T> boolean batchSet(final List<String> keys, final List<T> values, final int expiration, final boolean isHot, final String category)
			throws StoreException;
	
	<T> void asyncBatchSet(final List<String> keys, final List<T> values, final int expiration, final boolean isHot, final String category, 
	        final StoreCallback<Boolean> callback);

	// increment
	long increment(String key, int amount, String category) throws StoreException;

	// decrement
	long decrement(String key, int amount, String category) throws StoreException;

	void clear() throws StoreException;

	boolean isDistributed();

}
