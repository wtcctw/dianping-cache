package com.dianping.cache.redis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisClientFactory {

    private static Logger logger = LoggerFactory.getLogger(RedisClientFactory.class);
    
    private static final String KEY_REDIS_CONN_TIMEOUT = "avatar-cache.redis.conn.timeout";
    private static final int DEFAULT_REDIS_CONN_TIMEOUT = 2000;
    private static final String KEY_REDIS_READ_TIMEOUT = "avatar-cache.redis.read.timeout";
    private static final int DEFAULT_REDIS_READ_TIMEOUT = 2000;
    private static final String KEY_REDIS_MAX_REDIRECTS = "avatar-cache.redis.max.redirects";
    private static final int DEFAULT_REDIS_MAX_REDIRECTS = 3;
    
    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
        
    private static int connTimeout = configManager.getIntValue(KEY_REDIS_CONN_TIMEOUT, DEFAULT_REDIS_CONN_TIMEOUT);
    private static int readTimeout = configManager.getIntValue(KEY_REDIS_READ_TIMEOUT, DEFAULT_REDIS_READ_TIMEOUT);
    private static int maxRedirects = configManager.getIntValue(KEY_REDIS_MAX_REDIRECTS, DEFAULT_REDIS_MAX_REDIRECTS);
    
    public static JedisCluster createClient(RedisClientConfig config) {
        JedisCluster client = new JedisCluster(
                getClusterNodes(config.getServerList()),
                config.getConnTimeout(), 
                config.getReadTimeout(), 
                config.getMaxRedirects(), 
                getPoolConfig(config));
        return client;
    }

    private static Set<HostAndPort> getClusterNodes(List<String> serverList) {
        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        for(String server : serverList) {
            int idx = server.indexOf(':');
            if(idx == -1) {
                logger.error("invalid redis server: " + server);
                continue;
            }
            try {
                String ip = server.substring(0, idx);
                String port = server.substring(idx + 1);
                HostAndPort hp = new HostAndPort(ip, Integer.parseInt(port));
                nodes.add(hp);
            } catch(RuntimeException e) {
                logger.error("invalid reids server: " + server);
            }
        }
        return nodes;
    }

    private static GenericObjectPoolConfig getPoolConfig(RedisClientConfig config) {
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        conf.setMaxWaitMillis(1000);
        conf.setMaxTotal(25);
        conf.setMaxIdle(10);
        conf.setTestOnBorrow(false);
        conf.setTestOnReturn(false);
        conf.setTestWhileIdle(true);
        conf.setMinEvictableIdleTimeMillis(60000);
        conf.setTimeBetweenEvictionRunsMillis(30000);
        conf.setNumTestsPerEvictionRun(-1);
        return conf;
    }
    
}
