package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.StoreKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Set the specified hash field to the specified value if the field not exists. <b>Time
     * complexity:</b> O(1)
     * @return If the field already exists, 0 is returned, otherwise if a new field is created 1 is
     *         returned.
     */
    Long hsetnx(StoreKey key, final String field, final String value);
    Long hlen(StoreKey key);
    Boolean hExists(StoreKey key, String field);
}
