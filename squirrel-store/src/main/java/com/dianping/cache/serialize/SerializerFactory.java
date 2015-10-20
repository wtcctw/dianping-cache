package com.dianping.cache.serialize;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.cache.serialize.Serializer.SerializeType;

public class SerializerFactory {

private static ConcurrentMap<String, Serializer> serializerMap = new ConcurrentHashMap<String, Serializer>();
    
    public static Serializer getSerializer(String serialize) {
        if(serialize == null) {
            return null;
        }
        
        String key = serialize;
        Serializer serializer = serializerMap.get(key);
        if(serializer == null) {
            synchronized(SerializerFactory.class) {
                serializer = serializerMap.get(key);
                if(serializer == null) {
                    serializer = createSerializer(serialize);
                    serializerMap.put(key, serializer);
                }
            }
        }
        
        return serializer;
    }

    private static Serializer createSerializer(String serialize) {
        if(SerializeType.hessian.name().equalsIgnoreCase(serialize)) {
            return new HessianSerializer();
        } else if(SerializeType.json.name().equalsIgnoreCase(serialize)) {
            return new JsonSerializer();
        }
        throw new RuntimeException("unsupported serialize type: " + serialize);
    }
    
}
