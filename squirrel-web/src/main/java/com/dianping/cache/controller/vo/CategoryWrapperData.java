package com.dianping.cache.controller.vo;

import com.dianping.cache.entity.CacheKeyConfiguration;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/22.
 */
public class CategoryWrapperData {
    private CacheKeyConfiguration category;
    private long count;
    private long size;

    public CacheKeyConfiguration getCategory() {
        return category;
    }

    public void setCategory(CacheKeyConfiguration category) {
        this.category = category;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
