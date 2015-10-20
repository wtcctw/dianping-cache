package com.dianping.avatar.cache;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CacheKeyTest {

    @Test
    public void testHashCode() {
        CacheKey k1 = new CacheKey("category", 1);
        CacheKey k2 = new CacheKey("category", 1);
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
        Map<CacheKey, Object> map = new HashMap<CacheKey, Object>();
        map.put(k1, "k1");
        map.put(k2, "k2");
        assertEquals(map.size(), 1);
    }

    @Test
    public void testEqualsObject() {
        fail("Not yet implemented");
    }

}
