package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreKey;

import java.util.List;
import java.util.Set;

public interface RedisSetCommands {

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。 <br>
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。当 key 不是集合类型时，返回一个错误。<br>
     * @param key
     * @param member
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    Long sadd(StoreKey key, Object... member);
    
    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。当 key 不是集合类型，返回一个错误。
     * @param key
     * @param member
     * @return 被成功移除的元素的数量，不包括被忽略的元素。
     */
    Long srem(StoreKey key, Object... member);

    /**
     * 返回集合 key 中的所有成员。不存在的 key 被视为空集合。
     * @param key
     * @return 集合中的所有成员。
     */
    Set<Object> smembers(StoreKey key);

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     * @param key
     * @return 集合的基数。当 key 不存在时，返回 0 。
     */
    Long scard(StoreKey key);

    /**
     * 判断 member 元素是否集合 key 的成员。
     * @param key
     * @param member
     * @return 如果 member 元素是集合的成员，返回true。如果 member 元素不是集合的成员，或 key 不存在，返回false。
     */
    Boolean sismember(StoreKey key, Object member);
    
    /**
     * 移除并返回集合中的一个随机元素。
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。
     * @param key
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    Object spop(StoreKey key);

    /**
     * 如果count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，
     * 数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
     * @param key
     * @param count
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    Set<Object> spop(StoreKey key, long count);

    /**
     * 返回集合中的一个随机元素。
     * @param key
     * @return 只提供 key 参数时，返回一个元素；如果集合为空，返回 nil 。
     */
    Object srandmember(StoreKey key);

    /**
     * 如果count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，
     * 数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。
     * @param key
     * @param count
     * @return 那么返回一个数组；如果集合为空，返回空数组。
     */
    List<Object> srandmember(StoreKey key, int count);
    
}
