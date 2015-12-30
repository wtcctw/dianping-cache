package com.dianping.squirrel.client.util;

import java.util.HashMap;
import java.util.Map;

public class ParamHelper {

    private String paramString;
    private Map<String, String> paramMap = new HashMap<String, String>();

    public ParamHelper(String paramString) {
        if(paramString == null) 
            throw new NullPointerException("param string is null");
        this.paramString = paramString;
        parseParamString();
    }

    private void parseParamString() {
        String[] pairs = paramString.split("&");
        for(String pair : pairs) {
            String[] keyValue = pair.split("=");
            if(keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if(key.length() > 0 && value.length() > 0) {
                    paramMap.put(key, value);
                }
            }
        }
    }
    
    public String get(String key) {
        return paramMap == null ? null : paramMap.get(key);
    }
    
    public Integer getInteger(String key) {
        String value = get(key);
        return value == null ? null : Integer.parseInt(value);
    }
    
    public Long getLong(String key) {
        String value = get(key);
        return value == null ? null : Long.parseLong(value);
    }
    
    public Boolean getBoolean(String key) {
        String value = get(key);
        return value == null ? null : Boolean.parseBoolean(value);
    }

    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass) {
        String value = get(key);
        return value == null ? null : Enum.valueOf(enumClass, value);
    }
    
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }
    
    public Integer getInteger(String key, Integer defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
    
    public Long getLong(String key, Long defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Long.parseLong(value);
    }
    
    public Boolean getBoolean(String key, Boolean defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }
    
    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass, E defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : Enum.valueOf(enumClass, value);
    }
    
}

