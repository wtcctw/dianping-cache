package com.dianping.cache.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;

public class MemcachedClientFactory {

    private static MemcachedClientFactory instance = new MemcachedClientFactory();
    
    public static MemcachedClientFactory getInstance() {
        return instance;
    }
    
    private MemcachedClientFactory() {};
    
    private ConnectionFactory connFactory = new DefaultConnectionFactory() {
        public long getOperationTimeout() {
            return 50;
        }
    };
    
    private static Map<String, MemcachedClient> serverClientMap = 
            new HashMap<String, MemcachedClient>();
    
    public MemcachedClient getClient(String server) throws IOException {
        if(StringUtils.isBlank(server)) {
            throw new NullPointerException("server is null");
        }
        server = server.trim();
        MemcachedClient mc = serverClientMap.get(server);
        if(mc == null) {
            synchronized(serverClientMap) {
                mc = serverClientMap.get(server);
                if(mc == null) {
                    mc = createClient(server);
                    serverClientMap.put(server, mc);
                }
            }
        }
        return mc;
    }
    
    private MemcachedClient createClient(String server) throws IOException {
        MemcachedClient mc = new MemcachedClient(connFactory, AddrUtil.getAddresses(server));
        return mc;
    }

    public void close() {
        for(MemcachedClient mc : serverClientMap.values()) {
            mc.shutdown();
        }
    }
    
}
