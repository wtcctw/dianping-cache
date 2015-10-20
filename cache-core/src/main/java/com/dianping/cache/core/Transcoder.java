package com.dianping.cache.core;

import com.dianping.cache.serialize.SerializeException;

public interface Transcoder<D> {

    public <T> D encode(T object) throws SerializeException;
    
    public <T> T decode(D data, Class<T> clazz) throws SerializeException;
    
}
