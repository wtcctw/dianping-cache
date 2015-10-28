package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.core.Transcoder;
import com.dianping.squirrel.common.compress.Compressor;
import com.dianping.squirrel.common.compress.Compressor.CompressType;
import com.dianping.squirrel.common.compress.CompressorFactory;
import com.dianping.squirrel.common.exception.StoreTranscodeException;
import com.dianping.squirrel.common.serialize.SerializeException;
import com.dianping.squirrel.common.serialize.Serializer;
import com.dianping.squirrel.common.serialize.Serializer.SerializeType;
import com.dianping.squirrel.common.serialize.SerializerFactory;

/**
 * Redis serialize format:
 * 
 * long:    string representation of long value, used in increase/decrease operations
 * other:   $@[compress_flag][serialize_flag] + compressed/serialized content
 * 
 * @author enlight
 */
public class RedisStringTranscoder implements Transcoder<String> {

    private static final String KEY_SERIALIZE_TYPE = "squirrel.serialize.type";
    private static final String DEFAULT_SERIALIZE_TYPE = SerializeType.hessian.name();
    private static final String KEY_COMPRESS_ENABLE = "squirrel.compress.enable";
    private static final boolean DEFAULT_COMPRESS_ENABLE = false;
    private static final String KEY_COMPRESS_TYPE = "squirrel.compress.type";
    private static final String DEFAULT_COMPRESS_TYPE = CompressType.gzip.name();
    private static final String KEY_COMPRESS_THRESHOLD = "squirrel.compress.threshold";
    private static final int DEFAULT_COMPRESS_THRESHOLD = 16 * 1024;
    
    private static final String TRANSCODE_PREFIX = "$@";
    
    private static final byte SERIALIZE_INT = 1;
    private static final byte SERIALIZE_STRING = 1 << 1;
    private static final byte SERIALIZE_HESSIAN = 1 << 2;
    private static final byte SERIALIZE_JSON = 1 << 3;
    
    private static final byte COMPRESS_NONE = 0;
    private static final byte COMPRESS_GZIP = 1;
    private static final byte COMPRESS_SNAPPY = 2;
    
    private Serializer serializer;
    private Compressor compressor;

    public RedisStringTranscoder() {
        this.serializer = SerializerFactory.getSerializer(DEFAULT_SERIALIZE_TYPE);
        this.compressor = null;
    }

    @Override
    public <T> String encode(T object) {
        if(object instanceof Long) {
            return object.toString();
        }
        if(object instanceof Integer) {
            return TRANSCODE_PREFIX + (char)COMPRESS_NONE + (char)SERIALIZE_INT + 
                            object.toString();
        }
        if(object instanceof String) {
            return TRANSCODE_PREFIX + (char)COMPRESS_NONE + (char)SERIALIZE_STRING + 
                            (String)object;
        }
        try {
            String value = serializer.toString(object);
            return TRANSCODE_PREFIX + (char)COMPRESS_NONE + (char)SERIALIZE_HESSIAN + value;
        } catch (SerializeException e) {
            throw new StoreTranscodeException(e);
        }
    }

    @Override
    public <T> T decode(String data) {
        if(data.startsWith(TRANSCODE_PREFIX)) {
            byte compressType = (byte) data.charAt(2);
            if(compressType != 0) {
                throw new StoreTranscodeException("RedisStringTranscoder does not support compress");
            }
            byte serializeType = (byte) data.charAt(3);
            String value = data.substring(4);
            switch(serializeType) {
            case SERIALIZE_INT:
                return (T) Integer.valueOf(value);
            case SERIALIZE_STRING:
                return (T) value;
            case SERIALIZE_HESSIAN:
                try {
                    return (T) serializer.fromString(value);
                } catch (SerializeException e) {
                    throw new StoreTranscodeException(e);
                }
            default:
                throw new StoreTranscodeException("RedisStringTranscoder does not support serialize type: " + serializeType);
            }
        } else {
            return (T) Long.valueOf(data);
        }
    }
    
}
