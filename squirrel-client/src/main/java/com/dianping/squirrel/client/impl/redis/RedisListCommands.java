package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreKey;

import java.util.List;

public interface RedisListCommands {

	/**
	 * 将一个或多个值 value 插入到列表 key 的表尾(最右边)。如果有多个 value 值， 那么各个 value<br>
	 * 值按从左到右的顺序依次插入到表尾：比如对一个空列表 mylist 执行 RPUSH mylist a b c,得出的结果列表为 a b c<br>
	 * 等同于执行命令 RPUSH mylist a 、 RPUSH mylist b 、 RPUSH mylist c 。 <br>
	 * 如果 key 不存在，一个空列表会被创建并执行 RPUSH 操作。当 key 存在但不是列表类型时，返回一个错误。<br>
	 * 
	 * @return 执行 RPUSH 操作后，表的长度。
	 */
	Long rpush(StoreKey key, Object... value);

	/**
	 * 将一个或多个值 value 插入到列表 key 的表头。如果有多个 value 值，<br>
	 * 那么各个 value 值按从左到右的顺序依次插入到表头： 比如说，对空列表 mylist 执行命令 LPUSH mylist a b c <br>
	 * 列表的值将是 c b a ，这等同于原子性地执行 LPUSH mylist a、LPUSH mylist b和LPUSH mylist c 三个命令。<br>
	 * 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。当 key 存在但不是列表类型时，返回一个错误。<br>
	 * 
	 * @return 执行 LPUSH 命令后，列表的长度。
	 */
	Long lpush(StoreKey key, Object... value);

	/**
	 * 移除并返回列表 key 的头元素。
	 * @param key
	 * @return 列表的头元素。当 key 不存在时，返回 nil 。
	 */
	<T> T lpop(StoreKey key);

	/**
	 * 移除并返回列表 key 的尾元素。
	 * @param key
	 * @return 列表的尾元素。当 key 不存在时，返回 nil 。
	 */
	<T> T rpop(StoreKey key);

	/**
	 * 返回列表 key 中，下标为 index 的元素。下标(index)参数 start 和 stop 都以 0 为底，<br>
	 * 也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
	 * 如果 key 不是列表类型，返回一个错误。<br>
	 * 
	 * @return 列表中下标为 index 的元素。如果 index 参数的值不在列表的区间范围内(out of range)，返回 nil 。
	 */
	<T> T lindex(StoreKey key, long index);

	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value 。<br>
	 * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回错误。 <br>
	 * 
	 * @return 操作成功返回 true ，操作失败返回false，返回错误是抛出异常。
	 */
	Boolean lset(StoreKey key, long index, Object value);

	/**
	 * 返回列表 key 的长度。如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
	 * 如果 key 不是列表类型，返回一个错误。
	 * 
	 * @param key
	 * @return 列表 key 的长度。当发生错误时，抛出异常
	 */
	Long llen(StoreKey key);

	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。 <br>
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
	 * @param key
	 * @param start
	 * @param end
	 * @return 一个列表，包含指定区间内的元素。
	 */
	List<Object> lrange(StoreKey key, long start, long end);

	/**
	 * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
	 * 当 key 不是列表类型时，返回一个错误。
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return 命令执行成功时，返回true，否则返回false。如果发生错误抛出异常。
	 */
	Boolean ltrim(StoreKey key, long start, long end);

	/**
	 * 根据参数 count 的值，移除列表中与参数 value 相等的元素。
	 * count 的值可以是以下几种：
	 * 	count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
	 * 	count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
	 * 	count = 0 : 移除表中所有与 value 相等的值。
	 * @param key
	 * @param count
	 * @param value
	 * @return 被移除元素的数量。因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。
	 */
	Long lrem(StoreKey key, long count, Object value);

	/**
	 * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。
	 * 和 LPUSH 命令相反，当 key 不存在时， LPUSHX 命令什么也不做。
	 * @param key
	 * @param string
	 * @return LPUSHX 命令执行之后，表的长度。
	 */
	Long lpushx(StoreKey key, final Object... string);

	/**
	 * 将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表。
	 * 和 RPUSH 命令相反，当 key 不存在时， RPUSHX 命令什么也不做。
	 * @param key
	 * @param string
	 * @return RPUSHX 命令执行之后，表的长度。
	 */
	Long rpushx(StoreKey key, final Object... string);
}
