package com.dianping.cache.controller.vo;

/**
 * dp.wang@dianping.com
 * Created by dp on 16/1/18.
 */
public class AuthParams {
    String resource;
    String password;
    boolean strict;
    String application;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
