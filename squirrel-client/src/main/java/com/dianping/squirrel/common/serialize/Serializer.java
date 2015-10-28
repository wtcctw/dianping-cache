package com.dianping.squirrel.common.serialize;

public interface Serializer {

    public enum SerializeType {hessian, json, /*fst, protostuff*/};
    
    public String toString(Object object) throws SerializeException;

    public byte[] toBytes(Object object) throws SerializeException;
    
    public Object fromString(String data) throws SerializeException;

    public Object fromBytes(byte[] data) throws SerializeException;
    
}
