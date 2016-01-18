package com.dianping.squirrel.entity;

/**
 * Created by dp on 16/1/9.
 */
public class Auth {
    int id;
    String resource;
    String applications;
    String password;
    boolean strict;

    public Auth(){

    }
    public Auth(String resource){
        this.resource = resource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplications() {
        return applications;
    }

    public void setApplications(String applications) {
        this.applications = applications;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "id=" + id +
                ", resource='" + resource + '\'' +
                ", applications='" + applications + '\'' +
                ", password='" + password + '\'' +
                ", strict=" + strict +
                '}';
    }
}
