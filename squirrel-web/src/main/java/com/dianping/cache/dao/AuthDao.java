package com.dianping.cache.dao;

import com.dianping.cache.entity.Auth;

/**
 * Created by dp on 16/1/9.
 */
public interface AuthDao {
    void insert(Auth auth);
    void update(Auth auth);
    void delete(Auth auth);
}
