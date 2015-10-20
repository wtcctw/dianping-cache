package com.dianping.avatar.cache.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.dianping.cache.util.JsonUtils;
import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;
import com.dianping.remote.cache.util.SedesUtils;
import com.fasterxml.jackson.core.type.TypeReference;

public class SedesUtilsTest {

    @Test
    public void testSerialize() throws Exception {
        List<SingleCacheRemoveDTO> messages = generateCacheRemoveMessages("myehcache", 100);
        String message = SedesUtils.serialize(messages);
        System.out.println(message);
        messages = SedesUtils.deserialize(message);
        String json = JsonUtils.toStr(messages);
        System.out.println(json);
        System.out.println(message.length() + ":" + json.length());
        for(SingleCacheRemoveDTO msg : messages) {
            System.out.println(msg);
        }
    }

    @Test
    public void testPerformance() throws Exception {
        long start = System.currentTimeMillis();
        int msgCount = 10;
        int cycleTimes = 10000;
        List<SingleCacheRemoveDTO> messages = generateCacheRemoveMessages("myehcache", msgCount);
        for(int i=0; i<cycleTimes; i++) {
            String message = SedesUtils.serialize(messages);
        }
        long middle = System.currentTimeMillis();
        for(int i=0; i<cycleTimes; i++) {
            String message = JsonUtils.toStr(messages);
        }
        long end = System.currentTimeMillis();
        System.out.println("serialize: " + (middle-start) + ":" + (end-middle));
        String message = SedesUtils.serialize(messages);
        start = System.currentTimeMillis();
        for(int i=0; i<cycleTimes; i++) {
            List<SingleCacheRemoveDTO> msgs = SedesUtils.deserialize(message);
        }
        middle = System.currentTimeMillis();
        message = JsonUtils.toStr(messages);
        for(int i=0; i<cycleTimes; i++) {
            List<SingleCacheRemoveDTO> msgs = JsonUtils.fromStr(message, new TypeReference<List<SingleCacheRemoveDTO>>() {});
        }
        end = System.currentTimeMillis();
        System.out.println("deserialize: " + (middle-start) + ":" + (end-middle));
    }
    
    private List<SingleCacheRemoveDTO> generateCacheRemoveMessages(String category, int count) {
        List<SingleCacheRemoveDTO> messages = new ArrayList<SingleCacheRemoveDTO>(count);
        for(int i=0; i<count; i++) {
            messages.add(generateCacheRemoveMessage(category));
        }
        return messages;
    }
    
    private SingleCacheRemoveDTO generateCacheRemoveMessage(String category) {
        SingleCacheRemoveDTO message = new SingleCacheRemoveDTO();
        message.setCacheType("web");
        message.setCacheKey(category + "." + RandomStringUtils.randomAlphabetic(8) + "_10");
        return message;
    }
    
}
