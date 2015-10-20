package com.dianping.cache.scale.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.dianping.cache.scale.Node;
import com.dianping.cache.scale.ScaleException;

public class RedisClusterTest {

    @Test
    public void testLoadClusterInfo() throws Exception {
        List<String> serverList = new ArrayList<String>();
        serverList.add("192.168.224.149:7000");
        RedisCluster cluster = new RedisCluster(serverList);
        cluster.loadClusterInfo();
        List<RedisNode> nodes = cluster.getNodes();
        for(RedisNode node : nodes) {
            System.out.println(node);
        }
    }
    
    @Test
    public void testIsClusterEnabled() throws Exception {
        boolean enabled = RedisScalePlan.isClusterEnabled("192.168.224.149:7000");
        assertTrue(enabled);
    }

}
