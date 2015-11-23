package com.dianping.cache.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.dianping.cache.test.json.JsonSerializeException;
import com.dianping.cache.test.json.JsonSerializer;
import com.dianping.cache.test.json.impl.FastjsonSerializer;
import com.dianping.cache.test.json.impl.JacksonSerializer;
import com.dianping.cache.test.json.impl.JsonioSerializer;

public class JsonSedesTest {

    private JsonSerializer jsonio = new JsonioSerializer();
    private JsonSerializer fastjson = new FastjsonSerializer();
    private JsonSerializer jackson = new JacksonSerializer();
    
    private JsonSerializer current = jsonio;
    
    @Test
    public void testSedes() throws JsonSerializeException {
        testInteger(12345678);
        testLong(1234567812345678L);
        testString("hello baby~");
        Type t1 = new Type(1, "1");
        Type t2 = new Type(2, "2");
        Type t3 = new Type(3, "3");
        testObject(t2);
        Type[] array = {t1, t2, t3};
        testArray(array);
        Map<String, Type> map = new HashMap<String, Type>();
        map.put("key1", t1);
        map.put("key2", t2);
        map.put("key3", t3);
        testMapStringObject(map);
        Map<Type, Type> map2 = new HashMap<Type, Type>();
        map2.put(t1,  t1);
        map2.put(t2,  t2);
        map2.put(t3,  t3);
        testMapObjectObject(map2);
    }
    
    @Test
    public void testInteger(int number) throws JsonSerializeException {
//        String json = current.serialize(number);
//        System.out.println("integer " + number + " => " + json);
//        int number2 = current.deserialize(json);
//        assertEquals(number, number2);
    }
    
    @Test
    public void testLong(long number) throws JsonSerializeException {
//        String json = current.serialize(number);
//        System.out.println("long " + number + " => " + json);
//        long number2 = current.deserialize(json);
//        assertEquals(number, number2);
    }
    
    @Test
    public void testString(String string) throws JsonSerializeException {
        String json = current.serialize(string);
        System.out.println("string " + string + " => " + json);
        String string2 = current.deserialize(json);
        assertEquals(string, string2);
    }
    
    @Test
    public <T> void testObject(T object) throws JsonSerializeException {
        String json = current.serialize(object);
        System.out.println("object " + object + " => " + json);
        T object2 = current.deserialize(json);
        assertEquals(((Type)object).getValue(), ((Type)object2).getValue());
    }
    
    @Test
    public <T> void testArray(T[] array) throws JsonSerializeException {
        String json = current.serialize(array);
        System.out.println("array " + array + " => " + json);
        T[] array2 = current.deserialize(json);
        assertEquals(((Type[])array).length, ((Type[])array2).length);
        assertEquals(((Type[])array)[0].getValue(), ((Type[])array2)[0].getValue());
    }
    
    @Test
    public <T> void testMapStringObject(Map<String, T> map) throws JsonSerializeException {
        String json = current.serialize(map);
        System.out.println("map " + map + " => " + json);
        Map<String, T> map2 = current.deserialize(json);
        assertEquals(map.size(), map2.size());
        assertEquals(((Type)map.get("key1")).getValue(), ((Type)map2.get("key1")).getValue());
    }
    
    @Test
    public <T, S> void testMapObjectObject(Map<T, S> map) throws JsonSerializeException {
        String json = current.serialize(map);
        System.out.println("map " + map + " => " + json);
        Map<T, S> map2 = current.deserialize(json);
        assertEquals(map.size(), map2.size());
        assertEquals(((Type)map.get(new Type(1, "1"))).getValue(), ((Type)map2.get(new Type(1, "1"))).getValue());        
    }
    
    @Test
    public void testJackson() throws JsonSerializeException {
        current = jackson;
        testSedes();
    }
    
    @Test
    public void testFastjson() throws JsonSerializeException {
        current = fastjson;
        testSedes();
    }
    
    @Test
    public void testJsonio() throws JsonSerializeException {
        current = jsonio;
        testSedes();
    }
    
    public static void main(String[] args) {
        JsonSedesTest test = new JsonSedesTest();
        try {
            test.testJackson();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            test.testJsonio();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
