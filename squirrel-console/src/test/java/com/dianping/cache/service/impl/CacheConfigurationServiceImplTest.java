package com.dianping.cache.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/config/spring/appcontext-*.xml")
public class CacheConfigurationServiceImplTest {

    @Autowired
    private CacheConfigurationServiceImpl configService;
    
    @Test
    public void testCanClearCategory() {
        boolean canClean = configService.canClearCategory("mymemcache", null);
        assertTrue(canClean);
    }

    @Test
    public void testIsProductEnvironment() {
        boolean isProduct = configService.isProductEnvironment();
        assertFalse(isProduct);
    }
    
    @Test
    public void isProjectName() {
        boolean isProject = configService.isProjectName("cache-server");
        assertTrue(isProject);
        isProject = configService.isProjectName("tuangou-web");
        assertTrue(isProject);
        isProject = configService.isProjectName("testGroup");
        assertFalse(isProject);
        isProject = configService.isProjectName("10.1.1.1");
        assertFalse(isProject);
    }

    @Test
    public void testIsDisabledCategory() {
        boolean isDisabled = configService.isDisabledCategory("mymemcache");
        assertFalse(isDisabled);
        isDisabled = configService.isDisabledCategory("oStaticFileMD5");
        assertTrue(isDisabled);
    }

    @Test
    public void testIsClearAll() {
        boolean isClearAll = configService.isClearAll("");
        assertTrue(isClearAll);
        isClearAll = configService.isClearAll(null);
        assertTrue(isClearAll);
        isClearAll = configService.isClearAll(" 全部");
        assertTrue(isClearAll);
        isClearAll = configService.isClearAll("cache-server");
        assertFalse(isClearAll);
        isClearAll = configService.isClearAll("testGroup4");
        assertFalse(isClearAll);
        isClearAll = configService.isClearAll("127.0.0.1");
        assertFalse(isClearAll);
    }

    @Test
    public void testGetClearDestinations() {
        List<String> dest = configService.getDestinations("testGroup");
        assertEquals(dest.size(), 1);
        dest = configService.getDestinations("testGroup2");
        assertEquals(dest.size(), 2);
        dest = configService.getDestinations("testGroup3");
        assertEquals(dest.get(0), "testGroup3");
        dest = configService.getDestinations(null);
        assertNull(dest);
        dest = configService.getDestinations(" ");
        assertNull(dest);
        dest = configService.getDestinations("全部");
        assertNull(dest);
        dest = configService.getDestinations("cache-server");
        if(configService.isProductEnvironment()) {
            assertEquals(dest.size(), 3);
        } else {
            assertNull(dest);
        }
        dest = configService.getDestinations("127.0.0.1");
        assertEquals(dest.get(0), "127.0.0.1");
        dest = configService.getDestinations("127.0.0.1， 10.128.3.4 ");
        assertEquals(dest.get(1), "10.128.3.4");
    }

    @Test
    public void testGetDevicesFromCmdb() {
        List<String> devices = configService.getDevicesFromCmdb("cache-server", "生产");
        assertEquals(devices.size(), 3);
        devices = configService.getDevicesFromCmdb("cache-server", "beta");
        assertEquals(devices.size(), 1);
        devices = configService.getDevicesFromCmdb("cache-server", "ppe");
        assertEquals(devices.size(), 1);
    }

}
