package com.dianping.cache.dao;

import com.dianping.cache.entity.CategoryBusinessInfo;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/2/19.
 */
public interface CategoryBusinessInfoDao {
    CategoryBusinessInfo find(String category);
    void insert(CategoryBusinessInfo categoryBusinessInfo);
    void delete(String category);
}
