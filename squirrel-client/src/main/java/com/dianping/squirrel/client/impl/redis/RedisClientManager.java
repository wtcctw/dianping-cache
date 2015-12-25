package com.dianping.squirrel.client.impl.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisClientManager {

    private static Logger logger = LoggerFactory.getLogger(RedisClientManager.class);
    
    private static final String KEY_REDIS_CONN_PARAMS = "squirrel.redis.conn.params";
    
    private static final String KEY_CONN_TIMEOUT = "connTimeout";
    private static final int DEFAULT_CONN_TIMEOUT = 2000;
    private static final String KEY_READ_TIMEOUT = "readTimeout";
    private static final int DEFAULT_READ_TIMEOUT = 1000;
    private static final String KEY_MAX_REDIRECTS = "maxRedirects";
    private static final int DEFAULT_MAX_REDIRECTS = 1;
    private static final String KEY_POOL_MAXTOTAL = "poolMaxTotal";
    private static final int DEFAULT_POOL_MAXTOTAL = 30;
    private static final String KEY_POOL_MAXIDLE = "poolMaxIdle";
    private static final int DEFAULT_POOL_MAXIDLE = DEFAULT_POOL_MAXTOTAL;
    private static final String KEY_POOL_WAITMILLIS = "poolWaitMillis";
    private static final int DEFAULT_POOL_WAITMILLIS = 500;
    private static final String KEY_ASYNC_CORESIZE = "asyncCoreSize";
    private static final int DEFAULT_ASYNC_CORESIZE = 4;
    private static final String KEY_ASYNC_MAXSIZE = "asyncMaxSize";
    private static final int DEFAULT_ASYNC_MAXSIZE = 20;
    private static final String KEY_ASYNC_QUEUESIZE = "asyncQueueSize";
    private static final int DEFAULT_ASYNC_QUEUESIZE = 160000;
    private static final String KEY_CLUSTER_UPDATE_INTERVAL = "clusterUpdateInterval";
    private static final int DEFAULT_CLUSTER_UPDATE_INTERVAL = 1800000;
    
    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private static RedisClientManager instance = new RedisClientManager();
    
    private volatile int connTimeout = DEFAULT_CONN_TIMEOUT;
    private volatile int readTimeout = DEFAULT_READ_TIMEOUT;
    private volatile int maxRedirects = DEFAULT_MAX_REDIRECTS;
    private volatile int poolMaxTotal = DEFAULT_POOL_MAXTOTAL;
    private volatile int poolMaxIdle = DEFAULT_POOL_MAXIDLE;
    private volatile int poolWaitMillis = DEFAULT_POOL_WAITMILLIS;
    private volatile int asyncCoreSize = DEFAULT_ASYNC_CORESIZE;
    private volatile int asyncMaxSize = DEFAULT_ASYNC_MAXSIZE;
    private volatile int asyncQueueSize = DEFAULT_ASYNC_QUEUESIZE;
    private volatile int clusterUpdateInterval = DEFAULT_CLUSTER_UPDATE_INTERVAL;
    
    private volatile String connString;
    
    private volatile RedisClientConfig clientConfig;
    
    private volatile JedisCluster client;
    
    private RedisClientManager() {
        init();
    }
    
    public static RedisClientManager getInstance() {
        return instance;
    }
    
    public void setClientConfig(RedisClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        renewClient();
    }
    
    public JedisCluster getClient() {
        return client;
    }
    
    public void closeClient() {
        if(client != null) {
            client.close();
        }
    }
    
    private synchronized void renewClient() {
        JedisCluster oldClient = client;
        JedisCluster newClient = createClient();
        if(newClient != null) {
            client = newClient;
            logger.info("renewed redis cluster client: " + connString);
            if(oldClient != null) {
                oldClient.close();
            }
        }
    }
    
    private void init() {
        try {
            connString = configManager.getStringValue(KEY_REDIS_CONN_PARAMS);
            parseConnString();

            configManager.registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(KEY_REDIS_CONN_PARAMS.equals(key)) {
                        logger.info("redis connection params changed: " + value);
                        connString = value;
                        parseConnString();
                        renewClient();
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
    }
    
    private void parseConnString() {
        if(connString != null) {
            Map<String, String> keyValue = new HashMap<String, String>(); 
            String[] pairs = connString.split("&");
            for(String pair : pairs) {
                String kv[] = pair.split("=");
                if(kv.length == 2) {
                    String k = kv[0].trim();
                    String v = kv[1].trim();
                    if(k.length() > 0 && v.length() >0) {
                        keyValue.put(k, v);
                    }
                }
            }
            if(keyValue.size() > 0) {
                connTimeout = getIntegerParam(keyValue, KEY_CONN_TIMEOUT, connTimeout);
                readTimeout = getIntegerParam(keyValue, KEY_READ_TIMEOUT, readTimeout);
                maxRedirects = getIntegerParam(keyValue, KEY_MAX_REDIRECTS, maxRedirects);
                poolMaxTotal = getIntegerParam(keyValue, KEY_POOL_MAXTOTAL, poolMaxTotal);
                poolMaxIdle = getIntegerParam(keyValue, KEY_POOL_MAXIDLE, poolMaxIdle);
                poolWaitMillis = getIntegerParam(keyValue, KEY_POOL_WAITMILLIS, poolWaitMillis);
                asyncCoreSize = getIntegerParam(keyValue, KEY_ASYNC_CORESIZE, asyncCoreSize);
                asyncMaxSize = getIntegerParam(keyValue, KEY_ASYNC_MAXSIZE, asyncMaxSize);
                asyncQueueSize = getIntegerParam(keyValue, KEY_ASYNC_QUEUESIZE, asyncQueueSize);
                clusterUpdateInterval = getIntegerParam(keyValue, KEY_CLUSTER_UPDATE_INTERVAL, clusterUpdateInterval);
            }
        }
    }

    private int getIntegerParam(Map<String, String> props, String key, int defaultValue) {
        String value = props.get(key);
        if(value != null) {
            try {
                return Integer.valueOf(value);
            } catch(Exception e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public JedisCluster createClient() {
        JedisCluster client = new JedisCluster(
                getClusterNodes(clientConfig.getServerList()),
                connTimeout, 
                readTimeout, 
                maxRedirects, 
                getPoolConfig(clientConfig));
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

    private GenericObjectPoolConfig getPoolConfig(RedisClientConfig config) {
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        // 设置获取连接的最大等待时间
        conf.setMaxWaitMillis(poolWaitMillis);
        // 设置最大连接数
        conf.setMaxTotal(poolMaxTotal);
        // 设置最大空闲连接数
        conf.setMaxIdle(poolMaxIdle);
        // 设置最小空闲连接数
        conf.setMinIdle(0);
        // 设置获取连接时不进行连接验证(通过 PoolableObjectFactory.validateObject() 验证连接是否有效)
        conf.setTestOnBorrow(false);
        // 设置退还连接时不进行连接验证(通过 PoolableObjectFactory.validateObject() 验证连接是否有效)
        conf.setTestOnReturn(false);
        // 设置连接空闲时进行连接验证
        conf.setTestWhileIdle(true);
        // 设置连接被回收前的最大空闲时间
        conf.setMinEvictableIdleTimeMillis(5*60000);
        // 设置检测线程的运行时间间隔
        conf.setTimeBetweenEvictionRunsMillis(60000);
        // 设置检测线程每次检测的对象数
        conf.setNumTestsPerEvictionRun(-1);
        return conf;
    }
    
}
