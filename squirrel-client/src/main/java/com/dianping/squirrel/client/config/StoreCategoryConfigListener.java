package com.dianping.squirrel.client.config;

public interface StoreCategoryConfigListener {

    public void configChanged(CacheKeyType categoryConfig);
    
    public void configRemoved(CacheKeyType categoryConfig);
    
}
