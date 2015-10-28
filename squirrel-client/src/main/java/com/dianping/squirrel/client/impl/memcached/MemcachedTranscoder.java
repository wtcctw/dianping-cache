package com.dianping.squirrel.client.impl.memcached;

import com.dianping.squirrel.client.core.Transcoder;
import com.dianping.squirrel.common.compress.Compressor.CompressType;
import com.dianping.squirrel.common.serialize.Serializer.SerializeType;

import net.spy.memcached.CachedData;

public class MemcachedTranscoder implements Transcoder<CachedData>{

    private static final String DEFAULT_SERIALIZE = SerializeType.hessian.name();
    private static final String DEFAULT_COMPRESS = CompressType.none.name();
    
    private String serialize;
    private String compress;

    public MemcachedTranscoder() {
        this(DEFAULT_SERIALIZE, DEFAULT_COMPRESS);
    }
    
    public MemcachedTranscoder(String serialize, String compress) {
        this.serialize = serialize;
        this.compress = compress;
    }

    @Override
    public <T> CachedData encode(T object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T decode(CachedData data) {
        // TODO Auto-generated method stub
        return null;
    }

}
