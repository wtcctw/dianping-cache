package com.dianping.squirrel.client.core;

import com.dianping.squirrel.client.auth.AuthException;

public interface Authorizable {

    public void authorize(String client, String resource) throws AuthException;
    
}
