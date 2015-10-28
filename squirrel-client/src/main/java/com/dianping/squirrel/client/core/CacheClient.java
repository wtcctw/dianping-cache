/**
 * Project: avatar-cache
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
package com.dianping.squirrel.client.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.squirrel.common.exception.StoreException;

/**
 * Consistent cache client interface for the general purpose of transparent
 * handling all third cache implementations.
 * 
 * @author danson.liu
 * @author jinhua.liang
 * 
 */
public interface CacheClient {

    Future<Boolean> asyncSet(String key, Object value, int expiration, boolean isHot, String category)
            throws StoreException;

    void asyncSet(String key, Object value, int expiration, boolean isHot, String category,
            StoreCallback<Boolean> callback);

    boolean set(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
            TimeoutException;

    boolean set(String key, Object value, int expiration, long timeout, boolean isHot, String category)
            throws StoreException, TimeoutException;

    /**
     * Add an object to the cache if it does not exist already
     * 
     * @param key
     *            the key under which this object should be added
     * @param value
     *            the object to cache
     * @param expiration
     *            expiration the expiration of this cached item, in seconds from
     *            now on
     * @throws StoreException
     * @throws TimeoutException
     */
    boolean add(String key, Object value, int expiration, boolean isHot, String category) throws StoreException,
            TimeoutException;

    boolean add(String key, Object value, int expiration, long timeout, boolean isHot, String category)
            throws StoreException, TimeoutException;

    Future<Boolean> asyncAdd(String key, Object value, int expiration, boolean isHot, String category)
            throws StoreException;

    void asyncAdd(String key, Object value, int expiration, boolean isHot, String category,
            StoreCallback<Boolean> callback);

    /**
     * Replace an object with the given value if there is already a value for
     * the given key
     * 
     * @param key
     *            the key under which this object should be added
     * @param value
     *            value the object to cache
     * @param expiration
     *            expiration expiration the expiration of this cached item, in
     *            seconds from now on
     */
    void replace(String key, Object value, int expiration, boolean isHot, String category) throws Exception;

    /**
     * Get with a single key
     * 
     * @param <T>
     *            cached item's type
     * @param key
     *            the key to get
     * @return the result from the cache (null if there is none)
     */
    <T> T get(final String key, Class dataType, final String category) throws Exception;

    <T> T get(final String key, Class dataType, final boolean isHot, final String category, final boolean timeoutAware)
            throws Exception;

    <T> Future<T> asyncGet(final String key, Class dataType, final boolean isHot, final String category) throws StoreException;

    <T> void asyncGet(final String key, Class dataType, final boolean isHot, final String category, final StoreCallback<T> callback);

    <T> void asyncBatchGet(final Collection<String> keys, Class dataType, final boolean isHot, final Map<String, String> categories,
            final StoreCallback<Map<String, T>> callback);
    
    <T> void asyncBatchSet(final List<String> keys, final List<T> values, final int expiration, final boolean isHot, final String category, 
            final StoreCallback<Boolean> callback);
    
    <T> boolean batchSet(final List<String> keys, final List<T> values, final int expiration, final boolean isHot, final String category)
            throws StoreException, TimeoutException;

    <T> Map<String, T> getBulk(Collection<String> keys, Class dataType, boolean isHot, Map<String, String> categories,
            boolean timeoutAware) throws Exception;

    /**
     * remove the given key from the cache
     * 
     * @param key
     *            item of the key to delete
     */
    Future<Boolean> asyncDelete(String key, boolean isHot, String category) throws StoreException;

    boolean delete(String key, boolean isHot, String category) throws StoreException, TimeoutException;

    boolean delete(String key, boolean isHot, String category, long timeout) throws StoreException, TimeoutException;

    /**
     * Atomic-increase cached data with specified key by specified amount, and
     * return the new value.(The method is optional for client implementation.)
     * 
     * @param key
     *            the key
     * @param amount
     *            the amount to increment
     * @return the new value (-1 if the key doesn't exist)
     */
    long increment(String key, int amount, String category) throws StoreException, TimeoutException;

    long increment(String key, int amount, String category, long def) throws StoreException, TimeoutException;

    /**
     * Atomic-decrement the cache data with amount.(The method is optional for
     * client implementation.)
     * 
     * @param key
     *            the key
     * @param amount
     *            the amount to decrement
     * @return the new value (-1 if the key doesn't exist)
     */
    long decrement(String key, int amount, String category) throws StoreException, TimeoutException;

    long decrement(String key, int amount, String category, long def) throws StoreException, TimeoutException;

    void clear() throws Exception;

    boolean isDistributed();

    <T> CASValue<T> gets(String key, String category) throws StoreException, TimeoutException;

    CASResponse cas(String key, long casId, Object value, String category) throws StoreException, TimeoutException;
}
