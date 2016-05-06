package com.dianping.squirrel.client.impl.redis;

import java.util.Map;
import java.util.Set;

import com.dianping.squirrel.client.StoreKey;

public interface RedisZSetCommands {

	/**
	 * 将一个 member 元素及其 score 值加入到有序集 key 当中。 <br>
	 * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。<br>
	 * score 值可以是整数值或双精度浮点数。如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。当 key 存在但不是有序集类型时，抛出一个异常。 <br>
	 * @param key
	 * @param score
	 * @param member
	 * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
	 */
	Long zadd(StoreKey key, double score, Object member);

	/**
	 * 将 多个 member 元素及其 score 值加入到有序集 key 当中。 <br>
	 * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。<br>
	 * score 值可以是整数值或双精度浮点数。如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。当 key 存在但不是有序集类型时，抛出一个异常。 <br>
	 * @param key
	 * @param score
	 * @param member
	 * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
	 */
	Long zadd(StoreKey key, Map<Object, Double> scoreMembers);

	/**
	 * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。当 key 存在但不是有序集类型时，抛出一个异常。
	 * @param key
	 * @param members
	 * @return 被成功移除的成员的数量，不包括被忽略的成员。
	 */
	Long zrem(StoreKey key, Object... members);

	/**
	 * 为有序集 key 的成员 member 的 score 值加上增量 increment 。<br>
	 * 可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。<br>
	 * 当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。<br>
	 * 当 key 不是有序集类型时，抛出一个异常。score 值可以是整数值或双精度浮点数。<br>
	 * @param key
	 * @param score
	 * @param member
	 * @return member 成员的新 score 值，以字符串形式表示。
	 */
	Double zincrby(StoreKey key, double score, Object member);

	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。<br>
	 * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。<br>
	 * 使用 ZREVRANK 命令可以获得成员按 score 值递减(从大到小)排列的排名。<br>
	 * @param key
	 * @param member
	 * @return 如果 member 是有序集 key 的成员，返回 member 的排名。如果 member 不是有序集 key 的成员，返回null 。
	 */
	Long zrank(StoreKey key, Object member);

	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序。<br>
	 * 排名以 0 为底，也就是说， score 值最大的成员排名为 0 。<br>
	 * 使用 ZRANK 命令可以获得成员按 score 值递增(从小到大)排列的排名。<br>
	 * @param key
	 * @param member
	 * @return如果 member 是有序集 key 的成员，返回 member 的排名。 如果 member 不是有序集 key 的成员，返回 nil 。
	 */
	Long zrevrank(StoreKey key, Object member);

	/**
	 * 返回有序集 key 的基数。当 key 存在且是有序集类型时，返回有序集的基数。当 key 不存在时，返回 0 。
	 * @param key
	 * @return
	 */
	Long zcard(StoreKey key);

	/**
	 * 返回有序集 key 中，成员 member 的 score 值。果 member 元素不是有序集 key 的成员，或 key 不存在，返回 null 。
	 * @param key
	 * @param member
	 * @return member 成员的 score 值
	 */
	Double zscore(StoreKey key, Object member);

	/**
	 * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。<br>
	 * 于参数 min 和 max 的详细使用方法，请参考 ZRANGEBYSCORE 命令。<br>
	 * @param key
	 * @param min
	 * @param max
	 * @return score 值在 min 和 max 之间的成员的数量
	 */
	Long zcount(StoreKey key, double min, double max);

	/**
	 * 返回有序集 key 中，指定区间内的成员。其中成员的位置按 score 值递增(从小到大)来排序。<br>
	 * 具有相同 score 值的成员按字典序(lexicographical order )来排列。如果你需要成员按 score 值递减(从大到小)来排列，请使用 ZREVRANGE 命令。<br>
	 * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
	 * 超出范围的下标并不会引起错误。
	 * @param key
	 * @param start
	 * @param end
	 * @return 指定区间内，带有 score 值(可选)的有序集成员的列表。
	 */
	Set<Object> zrange(StoreKey key, long start, long end);

	/**
	 * 返回有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。有序集成员按 score 值递增(从小到大)次序排列。<br>
	 * 具有相同 score 值的成员按字典序(lexicographical order)来排列(该属性是有序集提供的，不需要额外的计算)。<br>
	 * 可选的 LIMIT 参数指定返回结果的数量及区间(就像SQL中的 SELECT LIMIT offset, count )，注意当 offset 很大时，定位 offset 的操作<br>
	 * 可能需要遍历整个有序集，此过程最坏复杂度为 O(N) 时间。<br>
	 * 可选的 WITHSCORES 参数决定结果集是单单返回有序集的成员，还是将有序集成员及其 score 值一起返回。<br>
	 * @param key
	 * @param min
	 * @param max
	 * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
	 */
	Set<Object> zrangeByScore(StoreKey key, double min, double max);

	Set<Object> zrangeByScore(StoreKey key, double min, double max, int offset, int count);

	/**
	 * 返回有序集 key 中，指定区间内的成员。其中成员的位置按 score 值递减(从大到小)来排列。<br>
	 * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order)排列。<br>
	 * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGE 命令的其他方面和 ZRANGE 命令一样。<br>
	 * @param key
	 * @param start
	 * @param end
	 * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
	 */
	Set<Object> zrevrange(StoreKey key, long start, long end);

	/**
	 * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列。<br>
	 * 具有相同 score 值的成员按字典序的逆序(reverse lexicographical order )排列。<br>
	 * 除了成员按 score 值递减的次序排列这一点外， ZREVRANGEBYSCORE 命令的其他方面和 ZRANGEBYSCORE 命令一样。<br>
	 * @param key
	 * @param max
	 * @param min
	 * @return 指定区间内，带有 score 值(可选)的有序集成员的列表
	 */
	Set<Object> zrevrangeByScore(StoreKey key, double max, double min);

	Set<Object> zrevrangeByScore(StoreKey key, double max, double min, int offset, int count);

	/**
	 * 移除有序集 key 中，指定排名(rank)区间内的所有成员。区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。<br>
	 * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
	 * @param key
	 * @param start
	 * @param end
	 * @return 被移除成员的数量。
	 */
	Long zremrangeByRank(StoreKey key, long start, long end);

	/**
	 * 移除有序集 key 中，所有 score 值介于 start 和 end 之间(包括等于 start 或 end )的成员。
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return 被移除成员的数量。
	 */
	Long zremrangeByScore(StoreKey key, double start, double end);

}
