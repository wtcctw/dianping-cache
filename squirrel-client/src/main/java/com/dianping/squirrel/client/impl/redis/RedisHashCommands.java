package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisHashCommands {

	/**
	 * 将哈希表 key 中的域 field 的值设为 value。如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。<br>
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。<br>
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 。
	 * 		  如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0 。
	 */
	Long hset(StoreKey key, String field, Object value);

	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 * @param key
	 * @param field
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
	 */
	<T> T hget(StoreKey key, String field);

	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。如果给定的域不存在于哈希表，那么返回一个 nil 值。 <br>
	 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。<br>
	 * @param key
	 * @param fields
	 * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 */
	List<Object> hmget(StoreKey key, final String... fields);

	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。此命令会覆盖哈希表中已存在的域。
	 * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
	 * @param key
	 * @param valueMap
	 * @return 如果命令执行成功，返回true. 否则,返回false。
	 */
	Boolean hmset(StoreKey key, final Map<String, Object> valueMap);

	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
	 * @param key
	 * @param field
	 * @return 被成功移除的域的数量，不包括被忽略的域。
	 */
	Long hdel(StoreKey key, String... field);

	/**
	 * 返回哈希表 key 中的所有域。
	 * @param key
	 * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
	 */
	Set<String> hkeys(StoreKey key);

	/**
	 * 返回哈希表 key 中所有域的值。
	 * @param key
	 * @return 一个包含哈希表中所有值的表。当 key 不存在时，返回一个空表。
	 */
	List<Object> hvals(StoreKey key);

	/**
	 * 返回哈希表 key 中，所有的域和值。
	 * @param key
	 * @return 返回Map
	 */
	Map<String, Object> hgetAll(StoreKey key);

	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 。增量也可以为负数，相当于对给定域进行减法操作。 <br>
	 * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。<br>
	 * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。本操作的值被限制在 64 位(bit)有符号数字表示之内。<br>
	 * @param key
	 * @param field
	 * @param amount
	 * @return执行 HINCRBY 命令之后，哈希表 key 中域 field 的值。
	 */
	Long hincrBy(StoreKey key, String field, int amount);

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。 <br>
	 * 若域 field 已经存在，该操作无效。如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。   <br>
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return 设置成功，返回 1 。如果给定域已经存在且没有操作被执行，返回 0 。
	 */
	Long hsetnx(StoreKey key, final String field, final Object value);

	/**
	 * 返回哈希表 key 中域的数量。
	 * @param key
	 * @return 哈希表中域的数量。当 key 不存在时，返回 0 。
	 */
	Long hlen(StoreKey key);

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在。
	 * @param key
	 * @param field
	 * @return 如果哈希表含有给定域，返回true。如果哈希表不含有给定域，或 key 不存在，返回false。
	 */
	Boolean hExists(StoreKey key, String field);
}
