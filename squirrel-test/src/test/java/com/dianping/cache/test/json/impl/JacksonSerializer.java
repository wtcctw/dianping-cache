package com.dianping.cache.test.json.impl;

import java.io.IOException;

import com.dianping.cache.test.json.JsonSerializeException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JacksonSerializer implements com.dianping.cache.test.json.JsonSerializer {

    final ObjectMapper mapper;

    public JacksonSerializer() {
        mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@type");
        
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(Object.class, new JsonSerializer<Object>() {

            @Override
            public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeFieldName(mapper.writeValueAsString(value));
            }
            
        });
        
        module.addKeyDeserializer(Object.class, new KeyDeserializer() {

            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                return mapper.readValue(key, Object.class);
            }
            
        });
        
        mapper.registerModule(module);
    }
    
    @Override
    public <T> String serialize(T object) throws JsonSerializeException {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(String string) throws JsonSerializeException {
        try {
            return (T)mapper.readValue(string, Object.class);
        } catch (IOException e) {
            throw new JsonSerializeException(e);
        }
    }

}
