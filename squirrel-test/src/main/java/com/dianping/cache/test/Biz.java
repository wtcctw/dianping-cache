package com.dianping.cache.test;

import org.springframework.stereotype.Component;

import com.dianping.squirrel.client.annotation.Store;
import com.dianping.squirrel.client.annotation.StoreParam;

@Component
public class Biz {

    @Store(category="mymemcache")
    public String load(@StoreParam String key) {
        return "load:" + key;
    }

}
