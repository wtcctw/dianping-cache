package com.dianping.cache.test.json;

public interface JsonSerializer {

    <T> String serialize(T object) throws JsonSerializeException;
    
    <T> T deserialize(String string) throws JsonSerializeException;
    
}
