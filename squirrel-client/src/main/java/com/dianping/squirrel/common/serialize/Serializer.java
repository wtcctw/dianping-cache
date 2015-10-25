package com.dianping.squirrel.common.serialize;

public interface Serializer {

    public enum SerializeType {hessian, json, /*fst, protostuff*/};
    
    public String toString(Object object) throws StoreSerializeException;

    public byte[] toBytes(Object object) throws StoreSerializeException;
    
    public Object fromString(String data, Class clazz) throws StoreSerializeException;

    public Object fromBytes(byte[] data, Class clazz) throws StoreSerializeException;
    
}
