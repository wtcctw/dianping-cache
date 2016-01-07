package com.dianping.squirrel.client.auth;

public interface AuthProvider {

    public boolean isStrict(String resource) throws AuthException;

    public boolean authorize(String client, String resource) throws AuthException;

}
