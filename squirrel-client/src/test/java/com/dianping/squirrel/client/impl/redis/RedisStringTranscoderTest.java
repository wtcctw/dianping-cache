package com.dianping.squirrel.client.impl.redis;

import com.dianping.squirrel.client.impl.Bean;
import com.dianping.squirrel.common.serialize.HessianSerializer;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class RedisStringTranscoderTest {

    @Test
    public void testAll() throws Exception {
        RedisStringTranscoder transcoder = new RedisStringTranscoder();
        HessianSerializer hs = new HessianSerializer();
        Bean bean = new Bean(12345678, "paasbean");
        // long
        String output = transcoder.encode(12345678L);
        System.out.println("long: " + output);
        assertEquals(output, "12345678");
        Long ll = transcoder.decode(output);
        assertEquals(12345678L, ll.longValue());
        // integer
        output = transcoder.encode(12345678);
        System.out.println("integer: " + output);
        printHex(output);
        System.out.println();
        Integer ii = transcoder.decode(output);
        assertEquals(12345678, ii.intValue());
        // string
        output = transcoder.encode("12345678");
        System.out.println("string: " + output);
        printHex(output);
        System.out.println();
        String ss = transcoder.decode(output);
        assertEquals("12345678", ss);
        // object
        output = transcoder.encode(bean);
        System.out.println("object: " + output);
        printHex(output);
        System.out.println();
        Bean bb = transcoder.decode(output);
        assertEquals(bean, bb);
    }

    @Test
    public void testEncode() {
    }

    @Test
    public void testDecode() {
    }

    public void printHex(String string) throws UnsupportedEncodingException {
        byte[] bytes = string.getBytes("ISO8859-1");
        printHex(bytes);
    }
    
    public void printHex(byte[] bytes) throws UnsupportedEncodingException {
        for(byte b : bytes ) {
            System.out.print(String.format("0x%02x ", b));
        }
    }
    
}
