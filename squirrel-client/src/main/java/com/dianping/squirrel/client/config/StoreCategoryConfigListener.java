package com.dianping.squirrel.client.config;

public interface StoreCategoryConfigListener {

    public void configChanged(StoreCategoryConfig categoryConfig);
    
    public void configRemoved(StoreCategoryConfig categoryConfig);
    
}
