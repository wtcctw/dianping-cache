package com.dianping.squirrel.common.serialize;

import static org.junit.Assert.*;

import org.junit.Test;

public class HessianSerializerTest {

    HessianSerializer ser = new HessianSerializer();
    
    @Test
    public void testToBytes() {
        byte[] bytes = ser.toBytes("hello");
        String str = ser.toString("hello");
        System.out.println(str);
    }

    @Test
    public void testToStringObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testFromBytes() {
        fail("Not yet implemented");
    }

    @Test
    public void testFromString() {
        fail("Not yet implemented");
    }

}
