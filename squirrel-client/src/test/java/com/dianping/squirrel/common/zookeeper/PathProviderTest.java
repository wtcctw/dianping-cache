package com.dianping.squirrel.common.zookeeper;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathProviderTest {

    @Test
    public void testPathProvider() {
        PathProvider pp = new PathProvider();
        pp.addTemplate("root", "/dp/cache/auth");
        pp.addTemplate("resource", "/dp/cache/auth/$0");
        pp.addTemplate("applications", "/dp/cache/auth/$0/applications");
        String path = pp.getPath("root");
        assertEquals(path, "/dp/cache/auth");
        path = pp.getPath("resource", "redis-test");
        assertEquals(path, "/dp/cache/auth/redis-test");
        path = pp.getPath("applications", "redis-test");
        assertEquals(path, "/dp/cache/auth/redis-test/applications");
        try {
            path = pp.getPath("template", "param");
            assertTrue(false);
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testPathProviderString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetRootPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testSetRootPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testAddTemplate() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetTemplates() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetTemplate() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetPath() {
        fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        fail("Not yet implemented");
    }

}
