package com.dianping.cache.memcached;

import static org.junit.Assert.*;

import java.io.Serializable;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import org.junit.Test;

import com.dianping.squirrel.client.impl.memcached.FstTranscoder;
import com.dianping.squirrel.client.impl.memcached.HessianTranscoder;

public class FstTranscoderTest {

    private int loop = 100000;
    
    private Transcoder fstTranscoder = new FstTranscoder();
    private Transcoder hesTranscoder = new HessianTranscoder();
    
    @Test
    public void test() {
        doTestEncode("hello world!");
        doTestEncode(123456789);
        doTestEncode(12345678900000000L);
        doTestEncode(new Bean("Michael Deng", 112233, "Mama said life was like a box of chocolates."));
    }

    private void doTestEncode(Object obj) {
        CachedData encoded = null;
        long m0 = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            encoded = fstTranscoder.encode(obj);
        }
        long m1 = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            fstTranscoder.decode(encoded);
        }
        long m2 = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            encoded = hesTranscoder.encode(obj);
        }
        long m3 = System.currentTimeMillis();
        for(int i=0; i<loop; i++) {
            hesTranscoder.decode(encoded);
        }
        long m4 = System.currentTimeMillis();
        System.out.println("fst: en" + (m1-m0) + " de" + (m2-m1) + " hes: en" + (m3-m2) +  " de" + (m4-m3));
    }

    public static class Bean implements Serializable {
        private String name;
        private int id;
        private String desc;

        public Bean() {};
        
        public Bean(String name, int id, String desc) {
            this.name = name;
            this.id = id;
            this.desc = desc;
        }
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
