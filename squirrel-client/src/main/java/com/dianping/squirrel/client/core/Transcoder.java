package com.dianping.squirrel.client.core;

public interface Transcoder<D> {

    public <T> D encode(T object);
    
    public <T> T decode(D data);
    
}
