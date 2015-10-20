package com.dianping.cache.monitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;

import org.apache.commons.lang.StringUtils;

public class MemcachedClientFactory {

    private static final int DEFAULT_PORT = 11211;
    
    private static Map<String, MemcachedClient> serverClientMap = 
            new HashMap<String, MemcachedClient>();
    
    public static MemcachedClient getMemcachedClient(String server) throws IOException {
        if(StringUtils.isBlank(server)) {
            throw new NullPointerException("server is null");
        }
        server = server.trim();
        MemcachedClient mc = serverClientMap.get(server);
        if(mc == null) {
            synchronized(serverClientMap) {
                mc = serverClientMap.get(server);
                if(mc == null) {
                    mc = createMemcachedClient(server);
                    serverClientMap.put(server, mc);
                }
            }
        }
        return mc;
    }

    private static MemcachedClient createMemcachedClient(String server) throws IOException {
        ServerInfo serverInfo = parseServer(server);
        MemcachedClient mc = new XMemcachedClient(serverInfo.ip, serverInfo.port);
        mc.setConnectTimeout(30000);
        mc.setOpTimeout(50);
        return mc;
    }

    private static ServerInfo parseServer(String server) {
        ServerInfo si = new ServerInfo();
        int idx = server.indexOf(':');
        if(idx == -1) {
            si.ip = server;
            si.port = DEFAULT_PORT;
        } else {
            si.ip = server.substring(0, idx);
            si.port = Integer.parseInt(server.substring(idx+1));
        }
        return si;
    }
    
    static class ServerInfo {
        String ip;
        int port;
    }
    
    public static void closeAll() {
        for(MemcachedClient mc : serverClientMap.values()) {
            try {
                mc.shutdown();
            } catch (IOException e) {
            }
        }
    }
}
