package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreKey;

import java.util.List;

public interface RedisListCommands {

    /**
     * Insert the values at the tail of the list stored at key
     * 
     * @return length of the list after the push operation
     */
    Long rpush(StoreKey key, Object... value);

    /**
     * Insert the values at the head of the list stored at key<br>
     * Value are inserted one after the other to the head of the list
     * 
     * @return the length of the list after the push operations
     */
    Long lpush(StoreKey key, Object... value);

    <T> T lpop(StoreKey key);

    <T> T rpop(StoreKey key);

    <T> T lindex(StoreKey key, long index);

    Boolean lset(StoreKey key, long index, Object value);

    /**
     * Returns the length of the list stored at key

     * @return the length of the list stored at key<br>
     *         0 if the key does not exist
     */
    Long llen(StoreKey key);

    /**
     * Returns the specified elements of the list stored at key
     *
     * @return list of elements in the specified range
     */
    List<Object> lrange(StoreKey key, long start, long end);

    /**
     * Trim an existing list so that it will contain only the specified range of elements
     * 
     * @return
     */
    Boolean ltrim(StoreKey key, long start, long end);

    Long lrem(StoreKey key, long count, Object value);

    Long lpushx(StoreKey key, final Object... string);

    Long rpushx(StoreKey key, final Object... string);
}
