package com.dianping.squirrel.client.impl.redis;

import redis.clients.jedis.JedisCluster;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.common.lifecycle.Locatable;

public interface RedisStoreClient
		extends StoreClient, Locatable, RedisListCommands, RedisHashCommands, RedisSetCommands, RedisZSetCommands {

	/**
	 * @return 是否存在该key，存在返回true，不存在返回false
	 */
	Boolean exists(StoreKey key);

	/**
	 * @return 返回该key的类型
	 */
	String type(StoreKey key);

	/**
	 * 
	 * @param key
	 * @param seconds 超时时间
	 * @return 设置成功返回true，当key不存在或者不能为key设置生存时间时返回false
	 */
	Boolean expire(StoreKey key, int seconds);

	/**
	 * @return 以秒为单位，返回给定 key 的剩余生存时间<br>
	 *         当 key 不存在时，返回 -2 。<br>
	 *         当 key 存在但没有设置剩余生存时间时，返回 -1 。<br>
	 */
	Long ttl(StoreKey key);

	/**
	 * 移除给定 key 的生存时间，将这个key从 易失的(带生存时间key) 转换成 持久的(一个不带生存时间、永不过期的key)
	 * 
	 * @return 当生存时间移除成功时，返回 true <br>
	 *         如果 key 不存在或 key 没有设置生存时间，返回 false <br>
	 */
	Boolean persist(StoreKey key);

	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
	 * 
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	Long append(StoreKey key, String value);

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。<br>
	 * 当 key 存在但不是字符串类型时，返回一个错误。
	 * 
	 * @return 返回给定 key 的旧值。当 key 不存在时，返回 null 。
	 */
	<T> T getSet(StoreKey key, Object value);

	/**
	 * 对 key 所储存的字符串值，获取指定偏移量上的位(bit)。 <br>
	 * 当 offset 比字符串值的长度大，或者 key 不存在时，返回 0 。<br>
	 * 
	 * @param key
	 * @param offset 偏移
	 * @return 字符串值指定偏移量上的位(bit)如果是1返回true，否则返回false
	 */
	Boolean getBit(StoreKey key, long offset);

	/**
	 * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。<br>
	 * 当 key 不存在时，自动生成一个新的字符串值。字符串会进行伸展(grown)以确保它可以将 value 保存在指定的偏移量上。<br>
	 * 当字符串值进行伸展时，空白位置以 0 填充。offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在 512 MB 之内)。<br>
	 * @param key
	 * @param offset 偏移
	 * @param value 设置的值
	 * @return 指定偏移量原来储存的值
	 */
	Boolean setBit(StoreKey key, long offset, boolean value);

	/**
	 * 计算给定字符串中，被设置为 1 的比特位的数量。
	 * @param key 
	 * @param start 起始位置 
	 * @param end   结束位置
	 * @return 字符串中设置为 1 的比特位的数量
	 */
	Long bitCount(StoreKey key, long start, long end);
	
	/**
	 * 计算给定字符串中，被设置为 1 的比特位的数量。
	 * @param key 
	 * @return 字符串中设置为 1 的比特位的数量
	 */
	Long bitCount(StoreKey key);

	
	Boolean setRaw(StoreKey key, Object value);

	<T> T getRaw(StoreKey key);

	JedisCluster getJedisClient();

}
