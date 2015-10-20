package com.dianping.cache.scale.impl;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

import com.dianping.cache.scale.Server;

public class RedisConnectionFactory {
    
    private static Map<String, Jedis> connPool = new HashMap<String, Jedis>();
    
    public static Jedis getConnection(String address) {
        Jedis jedis = connPool.get(address);
        if(jedis == null) {
            synchronized(RedisConnectionFactory.class) {
                jedis = connPool.get(address);
                if(jedis == null) {
                    jedis = createConnection(address);
                    connPool.put(address, jedis);
                }
            }
        }
        return jedis;
    }
    
    private static Jedis createConnection(String address) {
        Server server = new Server(address);
        return new Jedis(server.getIp(), server.getPort());
    }

}
