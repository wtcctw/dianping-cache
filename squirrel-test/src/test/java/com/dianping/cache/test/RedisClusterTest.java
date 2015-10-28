package com.dianping.cache.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RedisClusterTest {

    
    @Test
    public void testChar() throws Exception {
//        String string = "ŠØ";
        String string = "$@";
        string += (char)0;
        string += (char)4;
        string += (char)65;
        System.out.println(string);
        byte[] bytes = string.getBytes("UTF-8");
        for(byte b : bytes) {
            System.out.println(String.format("%02X", b));
        }
        for(int i=0; i<string.length(); i++) {
            System.out.println(String.format("%02X", (byte)string.charAt(i)));
        }
    }

}
