package com.dianping.cache.redis;

import com.dianping.cache.compress.Compressor;
import com.dianping.cache.compress.CompressorFactory;
import com.dianping.cache.compress.Compressor.CompressType;
import com.dianping.cache.core.Transcoder;
import com.dianping.cache.serialize.SerializeException;
import com.dianping.cache.serialize.Serializer;
import com.dianping.cache.serialize.Serializer.SerializeType;
import com.dianping.cache.serialize.SerializerFactory;

public class RedisTranscoder implements Transcoder<String> {

    private static final String DEFAULT_SERIALIZE = SerializeType.hessian.name();
    private static final String DEFAULT_COMPRESS = CompressType.none.name();
    private static final int DEFAULT_COMPRESS_THRESHOLD = 16 * 1024;
    private static final String COMPRESS_PREFIX = "*#@";
    
    private String serialize;
    private String compress;
    private Serializer serializer;
    private Compressor compressor;

    public RedisTranscoder() {
        this(DEFAULT_SERIALIZE, DEFAULT_COMPRESS);
    }
    
    public RedisTranscoder(String serialize, String compress) {
        this.serialize = serialize;
        this.compress = compress;
        this.serializer = SerializerFactory.getSerializer(serialize);
        this.compressor = CompressorFactory.getCompressor(compress);
    }

    @Override
    public <T> String encode(T object) throws SerializeException {
        if(object instanceof Integer) {
            return object.toString();
        }
        if(object instanceof Long) {
            return object.toString();
        }
        if(object instanceof String) {
            return (String)object;
        }
        String value = serializer.toString(object);
        return value;
    }

    @Override
    public <T> T decode(String data, Class<T> clazz) throws SerializeException {
        if(clazz == Integer.class) {
            return (T) Integer.valueOf(data);
        }
        if(clazz == Long.class) {
            return (T) Long.valueOf(data);
        }
        if(clazz == String.class) {
            return (T) data;
        }
        T object = serializer.fromString(data, clazz);
        return object;
    }
    
}
