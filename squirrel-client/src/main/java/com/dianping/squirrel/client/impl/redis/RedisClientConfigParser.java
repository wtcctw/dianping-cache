package com.dianping.squirrel.client.impl.redis;

import java.util.ArrayList;
import java.util.List;

import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.config.StoreClientConfigParser;
import com.dianping.squirrel.client.util.ParamHelper;

/**
 * Redis cluster URL: redis-cluster://ip1:port1,ip2:port2?param1=value1&param2=value2
 * 
 * @author enlight
 *
 */
public class RedisClientConfigParser implements StoreClientConfigParser {

    private static final String URL_PREFIX = "redis-cluster://";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_READ_TIMEOUT = "readTimeout";
    private static final String KEY_CONN_TIMEOUT = "connTimeout";
    private static final String KEY_MAX_REDIRECTS = "maxRedirects";
    private static final int DEFAULT_READ_TIMEOUT = 1000;
    private static final int DEFAULT_CONN_TIMEOUT = 1000;
    private static final int DEFAULT_MAX_REDIRECTS = 1;
    
    @Override
    public StoreClientConfig parse(CacheConfigurationDTO detail) {
        String url = detail.getServers();
        if(url == null || !url.startsWith(URL_PREFIX)) {
            return null;
        }
        RedisClientConfig config = new RedisClientConfig();
        
        String servers = url.substring(URL_PREFIX.length());
        String params = null;
        int idx = servers.indexOf('?');
        if(idx != -1) { 
            params = servers.substring(idx+1);
            servers = servers.substring(0, idx);
        }
        
        ParamHelper paramHelper = new ParamHelper(params);
        
        config.setPassword(paramHelper.get(KEY_PASSWORD));
        config.setReadTimeout(paramHelper.getInteger(KEY_READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        config.setConnTimeout(paramHelper.getInteger(KEY_CONN_TIMEOUT, DEFAULT_CONN_TIMEOUT));
        config.setMaxRedirects(paramHelper.getInteger(KEY_MAX_REDIRECTS, DEFAULT_MAX_REDIRECTS));
        config.setServerList(parseServers(servers));
        config.setClientClazz(detail.getClientClazz());

        return config;
    }

    private List<String> parseServers(String servers) {
        List<String> serverList = new ArrayList<String>();
        String[] array = servers.split(",");
        for(String s : array) {
            String trim = s.trim();
            if(trim.length() > 0) {
                serverList.add(trim);
            }
        }
        return serverList;
    }

}
