package com.dianping.squirrel.common.serialize;

public abstract  class AbstractSerializer implements Serializer {

    protected static final String CHARSET = "ISO8859-1";
    
    @Override
    public String toString(Object object) throws SerializeException {
        try {
            String string = doToString(object);
            afterToString(string);
            return string;
        } catch(Throwable t) {
            throw new SerializeException("failed to serialize object", t);
        }
    }

    protected String doToString(Object object) throws Exception {
        byte[] bytes = doToBytes(object);
        return new String(bytes, CHARSET);
    }

    @Override
    public byte[] toBytes(Object object) throws SerializeException {
        try {
            byte[] bytes = doToBytes(object);
            afterToBytes(bytes);
            return bytes;
        } catch(Throwable t) {
            throw new SerializeException("failed to serialize object", t);
        }
    }

    protected abstract byte[] doToBytes(Object object) throws Exception;
    
    @Override
    public Object fromString(String string) throws SerializeException {
        try {
            beforeFromString(string);
            Object object = doFromString(string);
            return object;
        } catch(Throwable t) {
            throw new SerializeException("failed to deserialize object", t);
        }
    }

    protected Object doFromString(String string) throws Exception {
        byte[] bytes = string.getBytes(CHARSET);
        return doFromBytes(bytes);
    }

    @Override
    public Object fromBytes(byte[] bytes) throws SerializeException {
        try {
            beforeFromBytes(bytes);
            Object object = doFromBytes(bytes);
            return object;
        } catch(Throwable t) {
            throw new SerializeException("failed to deserialize object", t);
        }
    }
    
    protected abstract Object doFromBytes(byte[] bytes) throws Exception;

    protected void afterToBytes(byte[] bytes) {};
    
    protected void afterToString(String string) {};
    
    protected void beforeFromBytes(byte[] bytes) {};
    
    protected void beforeFromString(String string) {};

}
