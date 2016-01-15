package com.dianping.squirrel.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.cache.dao.AuthDao;
import com.dianping.cache.entity.Auth;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.util.ParseServersUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.zookeeper.PathProvider;
import com.dianping.squirrel.common.zookeeper.ZookeeperClient;
import com.dianping.squirrel.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private static final String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private PathProvider pathProvider;
    private ZookeeperClient zkClient;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private CacheConfigurationService cacheConfigurationService;
    
    public AuthServiceImpl() throws Exception {
        String zkAddress = configManager.getStringValue(KEY_ZOOKEEPER_ADDRESS);
        if (zkAddress == null)
            throw new NullPointerException("squirrel zookeeper address is null");
        pathProvider = new PathProvider();
        pathProvider.addTemplate("root", "/dp/cache/auth");
        pathProvider.addTemplate("resource", "/dp/cache/auth/$0");
        pathProvider.addTemplate("applications", "/dp/cache/auth/$0/applications");
        zkClient = new ZookeeperClient(zkAddress);
        zkClient.start();
    }
    
    @Override
    public List<String> getApplications() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getResources() throws Exception {
        String path = pathProvider.getRootPath();
        List<String> children = zkClient.getChildren(path);
        return children;
    }

    @Override
    public void setStrict(String resource, boolean strict) throws Exception {
        String path = pathProvider.getPath("resource", resource);
        zkClient.set(path, String.valueOf(strict));
        syncAuthFromZK(resource);
    }

    @Override   
    public boolean getStrict(String resource) throws Exception {
        String path = pathProvider.getPath("resource", resource);
        String strict = zkClient.get(path);
        return Boolean.valueOf(strict);
    }

    @Override
    public void authorize(String application, String resource) throws Exception {
        List<String> appList = getAuthorizedApplications(resource);
        if(!appList.contains(application)) {
            appList.add(application);
        }
        String path = pathProvider.getPath("applications", resource);
        String apps = StringUtils.join(appList, ',');
        zkClient.set(path, apps);

        syncAuthFromZK(resource);
    }

    @Override
    public void unauthorize(String application, String resource) throws Exception {
        List<String> appList = getAuthorizedApplications(resource);
        if(appList.contains(application)) {
            appList.remove(application);
            String path = pathProvider.getPath("applications", resource);
            String apps = StringUtils.join(appList, ',');
            zkClient.set(path, apps);
            syncAuthFromZK(resource);
        }

    }

    @Override
    public List<String> getAuthorizedApplications(String resource) throws Exception {
        String path = pathProvider.getPath("applications", resource);
        String data = zkClient.get(path);
        if(data != null) {
            String [] apps = data.split(",");
            List<String> appList = new ArrayList<String>(8);
            for(String app : apps) {
                String trimedApp = app.trim();
                if(trimedApp.length() > 4) {
                    appList.add(trimedApp);
                }
            }
            return appList;
        }
        return new ArrayList<String>();
    }

    @Override
    public List<String> getAuthorizedResources(String application) {
        // TODO Auto-generated method stub
        return null;
    }

    private void syncAuthFromDB(String resource){
        Auth auth;
        auth = authDao.findByResource(resource);
        if(auth == null){
            return;
        }
    }

    private void syncAuthFromZK(String resource) throws Exception {
        Auth auth;
        auth = authDao.findByResource(resource);
        boolean strict = getStrict(resource);
        List<String> appList = getAuthorizedApplications(resource);
        CacheConfiguration cacheConfiguration = cacheConfigurationService.find(resource);
        Map<String,String> info = ParseServersUtil.parseRedisUrlInfo(cacheConfiguration.getServers());
        String password = info.get("password");
        String apps = StringUtils.join(appList, ',');
        if(null == auth){
            auth = new Auth(resource);
            auth.setApplications(apps);
            auth.setStrict(strict);
            auth.setPassword(password);
            authDao.insert(auth);
        }else{
            auth.setApplications(apps);
            auth.setStrict(strict);
            auth.setPassword(password);
            authDao.update(auth);
        }
    }
}
