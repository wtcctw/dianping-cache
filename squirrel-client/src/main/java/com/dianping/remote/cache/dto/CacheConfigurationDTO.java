/**
 * Project: avatar-cache-remote
 * 
 * File Created at 2010-10-18
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.remote.cache.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;

/**
 * Cache Client Configuration
 * 
 * @author danson.liu
 * 
 */
@JsonIgnoreProperties({"swimlane"})
public class CacheConfigurationDTO extends AbstractDTO {

    private static final long serialVersionUID = -4167929878555896829L;

    public static final String EHCACHE_CLIENT_CLAZZ = "com.dianping.cache.ehcache.EhcacheClientImpl";
    public static final String MEMCACHED_CLIENT_CLAZZ = "com.dianping.cache.memcached.MemcachedClientImpl";

    private static final String LIST_SEPARATOR = ";~;";

    private String cacheKey;

    private String clientClazz;

    private String servers;

    private String swimlane;

    private String transcoderClazz;

    private long addTime = System.currentTimeMillis();
    
    // For compatibility reason, reserve the two fields
    private String key;
    
    private CacheConfigDetailDTO detail;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getClientClazz() {
        return clientClazz;
    }

    public void setClientClazz(String clientClazz) {
        this.clientClazz = clientClazz;
    }

    public String getServers() {
        return servers;
    }

    public List<String> getServerList() {
        if (servers == null) {
            return null;
        }
        return Arrays.asList(servers.split(LIST_SEPARATOR));
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public void setServerList(List<String> serverList) {
        String servers = null;
        if (serverList != null && !serverList.isEmpty()) {
            servers = StringUtils.join(serverList, LIST_SEPARATOR);
        }
        setServers(servers);
    }

    public String getTranscoderClazz() {
        return transcoderClazz;
    }

    public void setTranscoderClazz(String transcoderClazz) {
        this.transcoderClazz = transcoderClazz;
    }
    
    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public CacheConfigDetailDTO getDetail() {
        return detail;
    }
    
    public void setDetail(CacheConfigDetailDTO detail) {
        this.detail = detail;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }
}
