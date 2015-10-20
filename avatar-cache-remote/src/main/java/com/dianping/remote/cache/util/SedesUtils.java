package com.dianping.remote.cache.util;

import java.util.ArrayList;
import java.util.List;

import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;

public class SedesUtils {

    // 'CC' for clear cache
    // cache key template: {category}.{index}_{version}
    public static final String MAGIC_HEADER = "*CC*";
    private static final String SEPARATOR = "*";
    private static final String CACHE_TYPE = "web";
    
    private static final String[] SpecialCategories = {
        "DianPing.Common.StaticFile",
        "DianPing.Common.CityDAC",
        "DianPing.Common.ConfigurationDAC",
        "DianPing.API.RegionAPIService_CustomerCategory",
    };
    
    public static String serialize(List<SingleCacheRemoveDTO> messages) {
        if(messages == null || messages.size() == 0) {
            return null;
        }
        
        StringBuilder buf = new StringBuilder(50+messages.size()*20);
        buf.append(MAGIC_HEADER);
        String category = getCategoryFromKey(messages.get(0).getCacheKey());
        buf.append(category);
        
        for(SingleCacheRemoveDTO message : messages) {
            String key = message.getCacheKey().substring(category.length() + 1);
            buf.append(SEPARATOR).append(key);
        }
        
        return buf.toString();
    }
    
    public static List<SingleCacheRemoveDTO> deserialize(String message) {
        if(message == null || !message.startsWith(MAGIC_HEADER)) {
            return null;
        }
        String[] keyArray = message.split("\\*");
        if(keyArray.length < 3) {
            return null;
        }
        
        List<SingleCacheRemoveDTO> messages = new ArrayList<SingleCacheRemoveDTO>(keyArray.length - 3);
        for(int i=3; i<keyArray.length; i++) {
            SingleCacheRemoveDTO msg = new SingleCacheRemoveDTO();
            msg.setCacheType(CACHE_TYPE);
            msg.setCacheKey(keyArray[2] + "." + keyArray[i]);
            messages.add(msg);
        }
        return messages;
    }
    
    public static String getCategoryFromKey(String key) {
        if(key == null) {
            return null;
        }
        int idx = key.indexOf('.');
        if(idx == -1) {
            return null;
        }
        String category = key.substring(0, idx);
        if(category.equals("DianPing")) {
            for(int i=0; i<SpecialCategories.length; i++) {
                if(key.startsWith(SpecialCategories[i])) {
                    category = SpecialCategories[i];
                }
            }
        }
        return category;
    }
    
}
