package com.dianping.cache.test.json.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dianping.cache.test.json.JsonSerializer;

public class FastjsonSerializer implements JsonSerializer {

    @Override
    public <T> String serialize(T object) {
        return JSON.toJSONString(object, SerializerFeature.WriteClassName);
    }

    @Override
    public <T> T deserialize(String string) {
        return (T)JSON.parse(string);
    }

}
