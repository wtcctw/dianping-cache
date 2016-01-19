package com.dianping.squirrel.client.impl.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.common.exception.StoreInitializeException;

public class RedisClientConfig implements StoreClientConfig {
    
    public static RedisClientConfig config;

    private String clientClazz;
    
    private List<String> serverList = new ArrayList<String>();
    
    private String password;
    
    private int connTimeout;
    
    private int readTimeout;
    
    private int maxRedirects;
    
    public String getClientClazz() {
        return this.clientClazz;
    }
    
    public void setClientClazz(String clientClazz) {
        this.clientClazz = clientClazz;
    }
    
    public List<String> getServerList() {
        return this.serverList;
    }

    public void setServerList(List<String> serverList) {
        this.serverList = serverList;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    @Override
    public void init() throws StoreInitializeException {
    }
    
    public String toString() {
        return new ToStringBuilder(this).
                append(serverList).
                append(password).
                append(connTimeout).
                append(readTimeout).
                append(maxRedirects).
                toString();
    }

}
