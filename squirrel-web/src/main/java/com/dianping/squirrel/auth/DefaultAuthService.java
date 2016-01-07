package com.dianping.squirrel.auth;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class DefaultAuthService implements AuthService {

    private static Logger logger = LoggerFactory.getLogger(DefaultAuthService.class);

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private CuratorFramework zkClient;
    
    public DefaultAuthService() {
    }
    
    @Override
    public List<String> getApplications() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getResources() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStrict(String resource, boolean strict) {
        // TODO Auto-generated method stub
        
    }

    @Override   
    public void getStrict(String resource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void authorize(String application, String resource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unauthorize(String application, String resource) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<String> getAuthorizedApplications(String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAuthorizedResources(String application) {
        // TODO Auto-generated method stub
        return null;
    }

}
