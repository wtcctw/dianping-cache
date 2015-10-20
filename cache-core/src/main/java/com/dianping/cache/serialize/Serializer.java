package com.dianping.cache.serialize;

public interface Serializer {

    public enum SerializeType {hessian, json, /*fst, protostuff*/};
    
    public <T> String toString(T object) throws SerializeException;

    public <T> byte[] toBytes(T object) throws SerializeException;
    
    public <T> T fromString(String data, Class<T> clazz) throws SerializeException;

    public <T> T fromBytes(byte[] data, Class<T> clazz) throws SerializeException;
    
}
