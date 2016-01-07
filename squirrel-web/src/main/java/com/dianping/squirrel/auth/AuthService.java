package com.dianping.squirrel.auth;

import java.util.List;

public interface AuthService {

    public List<String> getApplications();
    
    public List<String> getResources();
    
    public void setStrict(String resource, boolean strict);
    
    public void getStrict(String resource);
    
    public void authorize(String application, String resource);
    
    public void unauthorize(String application, String resource);
    
    public List<String> getAuthorizedApplications(String resource);
    
    public List<String> getAuthorizedResources(String application);
    
}
