package com.dianping.cache.test.json;

public interface JsonSerializer {

    public <T> String serialize(T object) throws JsonSerializeException;
    
    public <T> T deserialize(String string) throws JsonSerializeException;
    
}
