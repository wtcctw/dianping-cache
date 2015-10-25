package com.dianping.squirrel.client.core;

import com.dianping.squirrel.common.serialize.StoreSerializeException;

public interface Transcoder<D> {

    public <T> D encode(T object) throws StoreSerializeException;
    
    public <T> T decode(D data, Class<T> clazz) throws StoreSerializeException;
    
}
