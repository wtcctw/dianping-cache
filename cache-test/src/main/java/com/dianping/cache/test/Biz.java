package com.dianping.cache.test;

import org.springframework.stereotype.Component;

import com.dianping.avatar.cache.annotation.Cache;
import com.dianping.avatar.cache.annotation.CacheParam;

@Component
public class Biz {

    @Cache(category="mymemcache")
    public String load(@CacheParam String key) {
        return "load:" + key;
    }

}
