package com.dianping.cache.scale.cluster.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/19.
 */
public class JedisAuthWapper {

    private static Map<String,JedisPool> poolMap = new HashMap<String, JedisPool>();

    public static Jedis getJedis(RedisServer server){
        String address = server.getAddress();
        if(server.getRedisCluster() == null){
            RedisServer server1 = getServerInCluster(address);
            if(server1 != null){
                server = server1;
            }
        }
        JedisPool pool = getJedisPool(address);
        Jedis jedis = pool.getResource();
        if(server.getRedisCluster() != null){
            String password = server.getRedisCluster().getPassword();
            if(password != null && !password.equals("")){
                jedis.auth(password);
            }
        }
        return jedis;
    }

    public static Jedis getJedis(String address){
        RedisServer server = getServerInCluster(address);
        if(server != null){
            return getJedis(server);
        }
        return getJedisPool(address).getResource();
    }

    public static Jedis getJedis(String ip,int port){
        String address = ip+":"+port;
        RedisServer server = getServerInCluster(address);
        if(server != null){
            return getJedis(server);
        }
        return getJedisPool(address).getResource();
    }

    public static void returnResource(Jedis jedis){
        if(jedis != null){
            try {
                jedis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static JedisPool getJedisPool(String address){
        JedisPool jedisPool = poolMap.get(address);
        String[] hostAndPort = address.split(":");
        String ip = hostAndPort[0];
        int port = hostAndPort.length > 1 ? Integer.parseInt(hostAndPort[1]) : 6379;
        if(jedisPool == null){
            synchronized (poolMap){
                if(jedisPool == null){
                    jedisPool = new JedisPool(ip,port);
                    poolMap.put(address,jedisPool);
                }
            }
        }
        return jedisPool;
    }

    private static RedisServer getServerInCluster(String address){
        for(Map.Entry<String,RedisCluster> cluster : RedisManager.getClusterCache().entrySet()){
            if(cluster.getValue().getServer(address) != null){
                return cluster.getValue().getServer(address);
            }
        }
        return null;
    }
}
