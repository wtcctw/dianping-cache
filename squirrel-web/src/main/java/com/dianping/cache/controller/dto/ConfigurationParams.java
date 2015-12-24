package com.dianping.cache.controller.dto;

/**
 * Created by dp on 15/12/16.
 */
public class ConfigurationParams {

    private String cacheKey;

    private String clientClazz;

    private String servers;

    private String swimlane="";

    private String transcoderClazz;

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

    public void setServers(String servers) {
        this.servers = servers;
    }

    public String getSwimlane() {
        if(swimlane == null)
            return "";
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getTranscoderClazz() {
        return transcoderClazz;
    }

    public void setTranscoderClazz(String transcoderClazz) {
        this.transcoderClazz = transcoderClazz;
    }
}
