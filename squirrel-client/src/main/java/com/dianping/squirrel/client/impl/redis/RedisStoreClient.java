package com.dianping.squirrel.client.impl.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.squirrel.client.Store;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;

public interface RedisStoreClient extends StoreClient {

	// expire related
	Boolean exists(StoreKey key);

	String type(StoreKey key);

	Boolean expire(StoreKey key, int seconds);

	/**
	 * @return TTL in seconds<br>
	 *         -2 if key does not exist<br>
	 *         -1 if key exists but has no associated expire
	 */
	Long ttl(StoreKey key);

	/**
	 * Remove the existing timeout on key
	 * @param key
	 * @return 
	 * 		true if the timeout was removed<br>
	 * 		false if key does not exist or does not have an associated timeout.
	 */
	Boolean persist(StoreKey key);

	// hash related
	/**
	 * @return
	 *		1 if field is a new field in the hash and value was set<br>
  	 *      0 if field already exists in the hash and the value was updated
	 */
	Long hset(StoreKey key, String field, Object value);

	<T> T hget(StoreKey key, String field);

	/**
	 * @return the number of fields that were removed
	 */
	Long hdel(StoreKey key, String... field);

	Set<String> hkeys(StoreKey key);

	List<Object> hvals(StoreKey key);

	Map<String, Object> hgetAll(StoreKey key);

	// list related
	/**
	 * @return length of the list after the push operation
	 */
	Long rpush(StoreKey key, Object... value);

	Long lpush(StoreKey key, Object... value);

	<T> T lpop(StoreKey key);

	<T> T rpop(StoreKey key);

	<T> T lindex(StoreKey key, long index);

	Boolean lset(StoreKey key, long index, Object value);

	Long llen(StoreKey key);

	List<Object> lrange(StoreKey key, long start, long end);

	Boolean ltrim(StoreKey key, long start, long end);

	// set related
	/**
	 * @return number of elements that were added to the set
	 */
	Long sadd(StoreKey key, Object... member);
	
	/**
	 * @return number of members that were removed from the set
	 */
	Long srem(StoreKey key, Object... member);

	Set<Object> smembers(StoreKey key);

	Long scard(StoreKey key);

	Boolean sismember(StoreKey key, Object member);
	
	// sorted set related
	
}
