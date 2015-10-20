/**
 * Project: avatar
 * 
 * File Created at 2010-7-12
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.avatar.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cache.core.CASResponse;
import com.dianping.cache.core.CASValue;
import com.dianping.cache.core.CacheCallback;
import com.dianping.cache.core.CacheClient;
import com.dianping.cache.exception.CacheException;

/**
 * Cache interface provided for business.
 * 
 * @author guoqing.chen
 * 
 */
public interface CacheService {

	/**
	 * Use asyncSet instead
	 */
	@Deprecated
	boolean add(String key, Object value);

	/**
	 * Use asyncSet instead
	 */
	@Deprecated
	boolean add(String key, Object value, int expire);

	/**
	 * Use asyncSet instead
	 */
	boolean add(CacheKey key, Object value);

	/**
	 * 同步add，如果存在相同key则不增加，不存在相同key才会增加
	 * 
	 * @param key
	 * @param value
	 * @return 如果存在相同返回false，不存在相同key增加成功返回true
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	boolean addIfAbsent(CacheKey key, Object value) throws CacheException, TimeoutException;

	/**
	 * 同步add，如果存在相同key则不增加，不存在相同key才会增加
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 *            超时时间，毫秒
	 * @return 如果存在相同返回false，不存在相同key增加成功返回true
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	boolean addIfAbsent(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;

	/**
	 * Use asyncAddIfAbsent instead
	 */
	@Deprecated
	void addIfAbsentWithNoReply(CacheKey key, Object value) throws CacheException;

	/**
	 * 同步set，如果有相同key存在，也会覆盖相同key的内容
	 * 
	 * @param key
	 * @param value
	 * @return 如果成功返回true
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	boolean set(CacheKey key, Object value) throws CacheException, TimeoutException;

	/**
	 * 同步set，如果有相同key存在，也会覆盖相同key的内容
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 *            超时时间，毫秒
	 * @return 如果成功返回true
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	boolean set(CacheKey key, Object value, long timeout) throws CacheException, TimeoutException;

	/**
	 * Add entity to cache,CacheKey will be resolved by <tt>entity</tt>'s
	 * annotations
	 */
	<T> boolean add(T entity);

	/**
	 * Add entities to cache
	 */
	<T> boolean mAdd(List<T> entities);

	/**
	 * Add multiple instances,the <tt>cacheKey</tt> will be used to store
	 * instances keys
	 */
	<T> boolean mAdd(CacheKey cacheKey, List<T> objs);

	/**
	 * Retrieve cached item with specified simple key from 'default' cache
	 */
	@Deprecated
	<T> T get(String key);

	/**
	 * 获取指定key的value
	 * 
	 * @param key
	 * @return
	 */
	<T> T get(CacheKey key);

	/**
	 * Retrieve cached item by annotated class and parameters
	 */
	<T> T get(Class<?> cz, List<?> params);

	/**
	 * Retrieve all entity instance by class and parameters,it assume every
	 * Class only have one parameter.
	 */
	<T> List<T> mGet(Class<?> cz, List<?> params);

	/**
	 * Retrieve all entity instance by class and parameters
	 */
	<T> List<T> mGet(EntityKey... keys);

	/**
	 * Use mGetWithNonExists instead
	 */
	@Deprecated
	<T> List<T> mGet(List<CacheKey> keys);

	/**
	 * Retrieve cached items with keys cached by the specified CacheKey, returns
	 * null when any key missed if returnNullIfAnyKeyMissed been set to true,
	 * otherwise returns a value list as key order
	 */
	<T> List<T> mGet(final List<CacheKey> keys, final boolean returnNullIfAnyKeyMissed);

	/**
	 * 批量获取指定key集合的结果
	 * 
	 * @param keys
	 * @return 如果某个key没有查询到，则不会出现在结果里
	 */
	<T> Map<CacheKey, T> mGetWithNonExists(List<CacheKey> keys);

	/**
	 * Retrieve cached items with specified simple keys
	 */
	@Deprecated
	<T> Map<String, T> mGet(Set<String> keys);

	/**
	 * Retrieve cached items with cache key
	 */
	<T> List<T> mGet(CacheKey cacheKey);

	/**
	 * Remove cache with specified key Notice: for both java and .net app's
	 * cache
	 */
	boolean remove(CacheKey key);

	/**
	 * Remove item from specified cache with specified simple key Notice: only
	 * for java app's cache, except for .net's
	 */
	@Deprecated
	boolean remove(String cacheType, String key);

	/**
	 * Atomic-increase cached data with specified key by specified amount, and
	 * return the new value.(The method is optional for client implementation.)
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to increment
	 * @return the new value (-1 if the key doesn't exist)
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	public long increment(CacheKey key, int amount) throws CacheException, TimeoutException;

	public long increment(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;

	<T> CASValue<T> gets(CacheKey key) throws CacheException, TimeoutException;

	CASResponse cas(CacheKey key, long casId, Object value) throws CacheException, TimeoutException;

	/**
	 * Atomic-decrement the cache data with amount.(The method is optional for
	 * client implementation.)
	 * 
	 * @param key
	 *            the key
	 * @param amount
	 *            the amount to decrement
	 * @return the new value (-1 if the key doesn't exist)
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	public long decrement(CacheKey key, int amount) throws CacheException, TimeoutException;

	public long decrement(CacheKey key, int amount, long defaultValue) throws CacheException, TimeoutException;

	/**
	 * Generate final cache key string
	 * 
	 * @param key
	 * @return
	 */
	String getFinalKey(CacheKey key);

	/**
	 * 带超时的get接口，业务需要自己catch TimeouException
	 * 
	 * @param key
	 * @return
	 * @throws TimeoutException
	 */
	<T> T getOrTimeout(CacheKey key) throws TimeoutException;

	/**
	 * 带超时的get接口，业务需要自己catch TimeouException
	 * 
	 * @param keys
	 * @return
	 * @throws TimeoutException
	 */
	<T> Map<CacheKey, T> mGetOrTimeout(List<CacheKey> keys) throws TimeoutException;

	/**
	 * The composite key for multiple-get entities.
	 */
	class EntityKey {
		/**
		 * Entity class
		 */
		public final Class<?> cz;
		/**
		 * Parameters
		 */
		public final Object[] params;

		public EntityKey(Class<?> cz, Object... params) {
			this.cz = cz;
			this.params = params;
		}
	}

	/**
	 * 异步get接口，future方式
	 * 
	 * @param key
	 * @return 返回future对象
	 * @throws CacheException
	 */
	<T> Future<T> asyncGet(final CacheKey key) throws CacheException;

	/**
	 * 异步get接口，callback方式，memcached暂不支持
	 * 
	 * @param key
	 * @param callback
	 */
	<T> void asyncGet(final CacheKey key, final CacheCallback<T> callback);

	/**
	 * 异步批量get接口，callback方式，memcached暂不支持
	 * 
	 * @param keys
	 * @param callback
	 */
	<T> void asyncBatchGet(final List<CacheKey> keys, final CacheCallback<Map<CacheKey, T>> callback);

	<T> void asyncBatchSet(final List<CacheKey> keys, final List<T> values, final CacheCallback<Boolean> callback);
	
	<T> boolean batchSet(final List<CacheKey> keys, final List<T> values) throws CacheException, TimeoutException;
	
	/**
	 * 异步set接口，future方式
	 * 
	 * @param key
	 * @param value
	 * @return future
	 * @throws CacheException
	 */
	Future<Boolean> asyncSet(final CacheKey key, final Object value) throws CacheException;

	/**
	 * 异步set接口，callback方式
	 * 
	 * @param key
	 * @param value
	 * @param callback
	 */
	void asyncSet(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);

	/**
	 * 异步add接口，future方式
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws CacheException
	 */
	Future<Boolean> asyncAddIfAbsent(final CacheKey key, final Object value) throws CacheException;

	/**
	 * 异步add接口，callback方式
	 * 
	 * @param key
	 * @param value
	 * @param callback
	 */
	void asyncAddIfAbsent(final CacheKey key, final Object value, final CacheCallback<Boolean> callback);

	/**
	 * 同步delete接口
	 * 
	 * @param key
	 * @return 删除成功返回true
	 * @throws CacheException
	 * @throws TimeoutException
	 */
	boolean delete(CacheKey key) throws CacheException, TimeoutException;

	/**
	 * 异步delete接口，future方式
	 * 
	 * @param key
	 * @return
	 * @throws CacheException
	 */
	Future<Boolean> asyncDelete(CacheKey key) throws CacheException;

	CacheClient getCacheClient(CacheKey key);

	CacheClient getCacheClient(String cacheType);

	<T> T get(final CacheKey key, final String finalKey);
}
