package com.dianping.squirrel.client.impl.redis;

import redis.clients.jedis.JedisCluster;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.lifecycle.Locatable;

public interface RedisStoreClient extends StoreClient, Locatable, 
        RedisListCommands, RedisHashCommands, RedisSetCommands, RedisZSetCommands {

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

	Long append(StoreKey key, String value);

	<T> T getSet(StoreKey key, Object value);

	Boolean getBit(StoreKey key, long offset);

	Boolean setBit(StoreKey key, long offset, boolean value);

	Long bitCount(StoreKey key, long start, long end);

	Long bitCount(StoreKey key);

	Boolean setRaw(StoreKey key,Object value);

	<T> T getRaw(StoreKey key);

	JedisCluster getJedisClient();

}
