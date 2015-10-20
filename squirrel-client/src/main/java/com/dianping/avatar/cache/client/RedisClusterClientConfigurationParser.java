package com.dianping.avatar.cache.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cache.core.CacheClientConfiguration;
import com.dianping.cache.redis.RedisClientConfig;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;

/**
 * Redis cluster URL: redis-cluster://ip1:port1,ip2:port2?param1=value1&param2=value2
 * 
 * @author enlight
 *
 */
public class RedisClusterClientConfigurationParser implements CacheClientConfigurationParser {

    private static final String URL_PREFIX = "redis-cluster://";
    private static final String KEY_READ_TIMEOUT = "readTimeout";
    private static final String KEY_CONN_TIMEOUT = "connTimeout";
    private static final String KEY_MAX_REDIRECTS = "maxRedirects";
    private static final int DEFAULT_READ_TIMEOUT = 2000;
    private static final int DEFAULT_CONN_TIMEOUT = 2000;
    private static final int DEFAULT_MAX_REDIRECTS = 3;
    
    @Override
    public CacheClientConfiguration parse(CacheConfigurationDTO detail) {
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
        
        Map<String, String> paramMap = parseParams(params);
        String value = paramMap.get(KEY_READ_TIMEOUT);
        if(value != null) {
            config.setReadTimeout(Integer.parseInt(value));
        } else {
            config.setReadTimeout(DEFAULT_READ_TIMEOUT);
        }
        value = paramMap.get(KEY_CONN_TIMEOUT);
        if(value != null) {
            config.setConnTimeout(Integer.parseInt(value));
        } else {
            config.setConnTimeout(DEFAULT_CONN_TIMEOUT);
        }
        value = paramMap.get(KEY_MAX_REDIRECTS);
        if(value != null) {
            config.setMaxRedirects(Integer.parseInt(value));
        } else {
            config.setMaxRedirects(DEFAULT_MAX_REDIRECTS);
        }
        
        config.setServerList(parseServers(servers));

        config.setClientClazz(detail.getClientClazz());

        return config;
    }

    private List<String> parseServers(String servers) {
        List<String> serverList = new ArrayList<String>();
        String[] array = servers.split(",");
        for(String s : array) {
            serverList.add(s);
        }
        return serverList;
    }

    private Map<String, String> parseParams(String params) {
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] array = params.split("&");
        for(String param : array) {
            String[] pair = param.split("=");
            if(pair.length == 2) {
                paramMap.put(pair[0], pair[1]);
            }
        }
        return paramMap;
    }

}
