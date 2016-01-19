package com.dianping.squirrel.dao;

import com.dianping.squirrel.entity.Auth;

import java.util.List;

/**
 * Created by dp on 16/1/9.
 */
public interface AuthDao {
    
    void insert(Auth auth);

    void update(Auth auth);

    void delete(Auth auth);

    Auth findByResource(String resource);
}
