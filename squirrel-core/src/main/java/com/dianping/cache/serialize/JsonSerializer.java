package com.dianping.cache.serialize;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonSerializer implements Serializer {

    private static final String TypeField = "@type";
    
    private ObjectMapper mapper;
    
    public JsonSerializer() {
        init();
    }
    
    private void init() {
        mapper = new ObjectMapper();
        //设置输出时包含属性的风格
        mapper.setSerializationInclusion(Include.NON_NULL);
        //序列化时，忽略空的bean(即沒有任何Field)
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //序列化时，忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //序列化时，输出对象的类名
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, TypeField);
    }

    @Override
    public <T> String toString(T object) throws SerializeException {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new SerializeException("failed to serialize " + object, e);
        }
    }
    
    @Override
    public <T> byte[] toBytes(T object) throws SerializeException {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (IOException e) {
            throw new SerializeException("failed to serialize " + object, e);
        }
    }

    @Override
    public <T> T fromString(String data, Class<T> clazz) throws SerializeException {
        try {
            return (T)mapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new SerializeException("failed to deserialize " + data, e);
        }
    }
    
    @Override
    public <T> T fromBytes(byte[] data, Class<T> clazz) throws SerializeException {
        try {
            return (T)mapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new SerializeException("failed to deserialize " + data, e);
        }
    }

}
