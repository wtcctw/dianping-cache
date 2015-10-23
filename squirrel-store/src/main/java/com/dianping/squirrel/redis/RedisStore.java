package com.dianping.squirrel.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.squirrel.Store;

public interface RedisStore {

	// expire related
	Boolean exists(String key);

	String type(String key);

	Boolean expire(String key, int seconds);

	/**
	 * @return TTL in seconds
	 */
	Long ttl(String key);

	/**
	 * Remove the existing timeout on key
	 * @param key
	 * @return 
	 * 		true if the timeout was removed<br>
	 * 		false if key does not exist or does not have an associated timeout.
	 */
	Boolean persist(String key);

	// hash related
	/**
	 * @return
	 *		1 if field is a new field in the hash and value was set<br>
  	 *      0 if field already exists in the hash and the value was updated
	 */
	Long hset(String key, String field, Object value);

	<T> T hget(String key, String field);

	/**
	 * @return the number of fields that were removed
	 */
	Long hdel(String key, String... field);

	Set<String> hkeys(String key);

	List<Object> hvals(String key);

	Map<String, Object> hgetAll(String key);

	// list related
	/**
	 * @return length of the list after the push operation
	 */
	Long rpush(String key, Object... value);

	Long lpush(String key, Object... value);

	<T> T lpop(String key);

	<T> T rpop(String key);

	<T> T lindex(String key, long index);

	Boolean lset(String key, long index, Object value);

	Long llen(String key);

	List<Object> lrange(String key, long start, long end);

	Boolean ltrim(String key, long start, long end);

	// set related
	/**
	 * @return number of elements that were added to the set
	 */
	Long sadd(String key, Object... member);
	
	/**
	 * @return number of members that were removed from the set
	 */
	Long srem(String key, Object... member);

	Set<Object> smembers(String key);

	Long scard(String key);

	Boolean sismember(String key, Object member);
	
	// sorted set related
	
}
