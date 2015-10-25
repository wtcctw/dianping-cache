package com.dianping.cache.test;

import org.springframework.stereotype.Component;

import com.dianping.squirrel.client.annotation.Cache;
import com.dianping.squirrel.client.annotation.CacheParam;

@Component
public class Biz {

    @Cache(category="mymemcache")
    public String load(@CacheParam String key) {
        return "load:" + key;
    }

}
