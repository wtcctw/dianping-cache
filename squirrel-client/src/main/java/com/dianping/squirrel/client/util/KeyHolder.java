package com.dianping.squirrel.client.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.CacheKeyType;

public class KeyHolder {

    private CacheKeyType categoryConfig;
    
    private Map<String, StoreKey> keyMap;
    
    public KeyHolder(CacheKeyType categoryConfig, List<StoreKey> storeKeys) {
        checkNotNull(categoryConfig, "category config is null");
        this.categoryConfig = categoryConfig;
        checkNotNull(storeKeys, "store key list is null");
        this.keyMap = initKeyMap(storeKeys);
    }

    public List<String> getFinalKeys() {
        return new ArrayList<String>(keyMap.keySet());
    }
    
    public <T> Map<StoreKey, T> convertKeys(Map<String, T> from) {
        if(from ==  null || from.size() == 0)
            return Collections.EMPTY_MAP;
        Map<StoreKey, T> to = new HashMap<StoreKey, T>((int) (from.size() * 1.5));
        for(Map.Entry<String, T> entry : from.entrySet()) {
            StoreKey sk = keyMap.get(entry.getKey());
            if(sk != null) {
                to.put(sk, entry.getValue());
            }
        }
        return to;
    }
    
    private Map<String, StoreKey> initKeyMap(List<StoreKey> storeKeys) {
        Map<String, StoreKey> keyMap = new LinkedHashMap<String, StoreKey>();
        for(StoreKey sk : storeKeys) {
            checkNotNull(sk, "some store key is null");
            String fk = categoryConfig.getKey(sk.getParams());
            keyMap.put(fk, sk);
        }
        return keyMap;
    }

}
