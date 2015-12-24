package com.dianping.squirrel.client.impl.redis;

import java.util.List;
import java.util.Set;

import com.dianping.squirrel.client.StoreKey;

public interface RedisSetCommands {

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
    
    Object spop(StoreKey key);

    Set<Object> spop(StoreKey key, long count);

    Object srandmember(StoreKey key);

    List<Object> srandmember(StoreKey key, int count);
    
}
