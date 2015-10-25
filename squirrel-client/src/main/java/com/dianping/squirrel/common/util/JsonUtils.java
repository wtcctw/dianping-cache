package com.dianping.squirrel.common.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //设置输出时包含属性的风格
        MAPPER.setSerializationInclusion(Include.NON_NULL);
        //序列化时，忽略空的bean(即沒有任何Field)
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //序列化时，忽略在JSON字符串中存在但Java对象实际没有的属性
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 将对象转换为JSON格式
     * 
     * @param model
     * @return
     * @throws IOException
     */
    public static String toStr(Object model) throws IOException {
        return MAPPER.writeValueAsString(model);
    }

    /**
     * 将JSON字符串转换为指定类实例
     * 
     * @param <T>
     * @param content
     * @param clazz
     * @return
     * @throws IOException
     */
    public static <T> T fromStr(String content, Class<T> clazz) throws IOException {
        return MAPPER.readValue(content, clazz);
    }

    public static <T> T fromStr(String content, TypeReference<T> typeReference) throws IOException {
        return MAPPER.readValue(content, typeReference);
    }

}
