package com.dianping.squirrel.common.lifecycle;

import com.dianping.squirrel.client.auth.AuthException;

public interface Authorizable {

    public void authorize(String client, String resource) throws AuthException;
    
}
