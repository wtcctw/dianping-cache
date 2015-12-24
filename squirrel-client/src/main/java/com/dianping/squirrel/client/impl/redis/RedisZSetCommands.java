package com.dianping.squirrel.client.impl.redis;

import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Tuple;

import com.dianping.squirrel.client.StoreKey;

/*
 * Sort commands are not supported
 * Lex related commands are not supported
 * Min/max string representation is not supported now
 * Return object with score is not supported now
 */
public interface RedisZSetCommands {
    
    Long zadd(StoreKey key, double score, Object member);

    Long zadd(StoreKey key, Map<Object, Double> scoreMembers);

    Long zrem(StoreKey key, Object... members);

    Double zincrby(StoreKey key, double score, Object member);

    Long zrank(StoreKey key, Object member);

    Long zrevrank(StoreKey key, Object member);

    Long zcard(StoreKey key);

    Double zscore(StoreKey key, Object member);

    Long zcount(StoreKey key, double min, double max);

//    Long zcount(StoreKey key, String min, String max);
    
    Set<Object> zrange(StoreKey key, long start, long end);

    Set<Object> zrangeByScore(StoreKey key, double min, double max);

//    Set<Object> zrangeByScore(StoreKey key, String min, String max);

    Set<Object> zrangeByScore(StoreKey key, double min, double max, int offset, int count);
    
//    Set<Object> zrangeByScore(StoreKey key, String min, String max, int offset, int count);

//    Set<Tuple> zrangeWithScores(StoreKey key, long start, long end);
    
//    Set<Tuple> zrangeByScoreWithScores(StoreKey key, double min, double max);
    
//    Set<Tuple> zrangeByScoreWithScores(StoreKey key, String min, String max);
    
//    Set<Tuple> zrangeByScoreWithScores(StoreKey key, double min, double max, int offset, int count);
    
//    Set<Tuple> zrangeByScoreWithScores(StoreKey key, String min, String max, int offset, int count);
    
    Set<Object> zrevrange(StoreKey key, long start, long end);
    
    Set<Object> zrevrangeByScore(StoreKey key, double max, double min);

//    Set<Object> zrevrangeByScore(StoreKey key, String max, String min);

    Set<Object> zrevrangeByScore(StoreKey key, double max, double min, int offset, int count);
    
//    Set<Object> zrevrangeByScore(StoreKey key, String max, String min, int offset, int count);

//    Set<Tuple> zrevrangeWithScores(StoreKey key, long start, long end);
    
//    Set<Tuple> zrevrangeByScoreWithScores(StoreKey key, double max, double min);

//    Set<Tuple> zrevrangeByScoreWithScores(StoreKey key, String max, String min);

//    Set<Tuple> zrevrangeByScoreWithScores(StoreKey key, double max, double min, int offset, int count);

//    Set<Tuple> zrevrangeByScoreWithScores(StoreKey key, String max, String min, int offset, int count);

    Long zremrangeByRank(StoreKey key, long start, long end);

    Long zremrangeByScore(StoreKey key, double start, double end);

//    Long zremrangeByScore(StoreKey key, String start, String end);

}
