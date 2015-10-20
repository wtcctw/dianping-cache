package com.dianping.cache.test.json.impl;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.dianping.cache.test.json.JsonSerializer;

public class JsonioSerializer implements JsonSerializer {

    @Override
    public <T> String serialize(T object) {
        return JsonWriter.objectToJson(object);
    }

    @Override
    public <T> T deserialize(String json) {
        return (T)JsonReader.jsonToJava(json);
    }

}
