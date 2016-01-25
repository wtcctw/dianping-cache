package com.dianping.squirrel.service;

import java.util.List;

public interface AuthService {

    public List<String> getApplications() throws Exception;
    
    public List<String> getResources() throws Exception;
    
    public void setStrict(String resource, boolean strict) throws Exception;
    
    public boolean getStrict(String resource) throws Exception;
    
    public void authorize(String application, String resource) throws Exception;
    
    public void unauthorize(String application, String resource) throws Exception;
    
    public List<String> getAuthorizedApplications(String resource) throws Exception;
    
    public List<String> getAuthorizedResources(String application) throws Exception;

    public boolean setPassword(String resource,String password) throws Exception;

}
