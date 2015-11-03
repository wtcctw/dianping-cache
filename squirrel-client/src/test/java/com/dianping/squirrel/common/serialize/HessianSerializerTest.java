package com.dianping.squirrel.common.serialize;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dianping.squirrel.client.impl.Bean;

public class HessianSerializerTest {

    @Test
    public void testAll() throws Exception {
        HessianSerializer hs = new HessianSerializer();
        Bean bean = new Bean(12345678, "hello");
        String string = hs.toString(bean);
        Bean bean2 = (Bean) hs.fromString(string);
        assertEquals(bean, bean2);
    }
    
    @Test
    public void testDoToString() {
        fail("Not yet implemented");
    }

    @Test
    public void testDoToBytes() {
        fail("Not yet implemented");
    }

    @Test
    public void testDoFromString() {
        fail("Not yet implemented");
    }

    @Test
    public void testDoFromBytes() {
        fail("Not yet implemented");
    }

}
