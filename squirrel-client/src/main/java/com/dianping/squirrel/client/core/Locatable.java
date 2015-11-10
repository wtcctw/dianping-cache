package com.dianping.squirrel.client.core;

import com.dianping.squirrel.client.StoreKey;

public interface Locatable {

    public String locate(StoreKey storeKey);
    
    public String locate(String finalKey);
    
}
