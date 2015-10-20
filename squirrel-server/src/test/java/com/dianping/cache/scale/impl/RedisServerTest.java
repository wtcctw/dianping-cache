package com.dianping.cache.scale.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RedisServerTest {

    @Test
    public void testUpdateSlotList() {
        RedisServer server = new RedisServer("127.0.0.1:6379");
        List<Integer> slots = server.updateSlotList("3,3, 7 - 5,9 ,11-18,13-22,20,1000");
        System.out.println(Arrays.toString(slots.toArray()));
    }

    @Test
    public void testUpdateSlotString() {
        RedisServer server = new RedisServer("127.0.0.1:6379");
//        Integer[] slotArray = {7,3,3,5,6,11,12,15,18,16,17,1000,22,1000,21,9,13,14,19,20,14,14};
        Integer[] slotArray = {};
        String slotString = server.updateSlotString(Arrays.asList(slotArray));
        System.out.println(slotString);
    }

}
