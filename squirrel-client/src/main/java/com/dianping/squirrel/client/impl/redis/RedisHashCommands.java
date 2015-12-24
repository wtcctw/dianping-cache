package com.dianping.squirrel.client.impl.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.squirrel.client.StoreKey;

public interface RedisHashCommands {

    /**
     * @return
     *      1 if field is a new field in the hash and value was set<br>
     *      0 if field already exists in the hash and the value was updated
     */
    Long hset(StoreKey key, String field, Object value);

    <T> T hget(StoreKey key, String field);

    /**
     * @return list of values for the fields, if some field
     *         does not exist, a null value is in the returned list<br>
     *         null if the key does not exist or fields are not specified
     */
    List<Object> hmget(StoreKey key, final String... fields);
    
    Boolean hmset(StoreKey key, final Map<String, Object> valueMap);
    
    /**
     * @return the number of fields that were removed
     */
    Long hdel(StoreKey key, String... field);

    Set<String> hkeys(StoreKey key);

    List<Object> hvals(StoreKey key);

    Map<String, Object> hgetAll(StoreKey key);
    
    Long hincrBy(StoreKey key, String field, int amount);

}
