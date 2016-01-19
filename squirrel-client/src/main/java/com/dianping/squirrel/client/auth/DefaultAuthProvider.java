package com.dianping.squirrel.client.auth;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.NamedThreadFactory;
import com.dianping.squirrel.common.zookeeper.PathProvider;
import com.dianping.squirrel.common.zookeeper.ZookeeperClient;

public class DefaultAuthProvider implements AuthProvider {
    
    private static Logger logger = LoggerFactory.getLogger(DefaultAuthProvider.class);
    
    private static final String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private ZookeeperClient zkClient;
    private PathProvider pathProvider;

    private boolean globalStrict;
    private Map<String, Boolean> strictMap;
    private Map<String, Set<String>> resourceMap;
    
    public DefaultAuthProvider() throws Exception {
        String zkAddress = configManager.getStringValue(KEY_ZOOKEEPER_ADDRESS);
        if (zkAddress == null)
            throw new NullPointerException("squirrel zookeeper address is null");
        pathProvider = new PathProvider();
        pathProvider.addTemplate("root", "/dp/cache/auth");
        pathProvider.addTemplate("resource", "/dp/cache/auth/$0");
        pathProvider.addTemplate("applications", "/dp/cache/auth/$0/applications");
        zkClient = new ZookeeperClient(zkAddress);
        zkClient.start();
        initAuthData();
        startAuthDataSyncer();
    }

    private void initAuthData() throws Exception {
        globalStrict = getGlobalStrict();
        strictMap = loadStrictMap();
        resourceMap = loadResourceMap();
    }
    
    private void startAuthDataSyncer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("squirrel-auth-sync", true));
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    initAuthData();
                } catch (Exception e) {
                    logger.error("failed to sync auth data", e);
                }
            }}, 5, 5, TimeUnit.MINUTES);
    }

    private boolean getGlobalStrict() throws Exception {
        String value = zkClient.get(pathProvider.getRootPath());
        return "true".equalsIgnoreCase(value);
    }

    private Map<String, Boolean> loadStrictMap() throws Exception {
        Map<String, Boolean> strictMap = new HashMap<String, Boolean>();
        List<String> children = zkClient.getChildren(pathProvider.getRootPath());
        for(String child : children) {
            String value = zkClient.get(pathProvider.getPath("resource", child));
            strictMap.put(child, "true".equalsIgnoreCase(value));
        }
        return strictMap;
    }

    private Map<String, Set<String>> loadResourceMap() throws Exception {
        Map<String, Set<String>> resourceMap = new HashMap<String, Set<String>>();
        List<String> children = zkClient.getChildren(pathProvider.getRootPath());
        for(String child : children) {
            String data = zkClient.get(pathProvider.getPath("applications", child));
            if(data != null) {
                String [] apps = data.split(",");
                Set<String> appSet = new HashSet<String>();
                for(String app : apps) {
                    String trimedApp = app.trim();
                    if(trimedApp.length() > 4) {
                        appSet.add(trimedApp);
                    }
                }
            }
        }
        return resourceMap;
    }

    @Override
    public boolean isStrict(String resource) {
        if(resource == null) {
            throw new NullPointerException("resource is null");
        }
        Boolean strict = strictMap.get(resource);
        return strict == null ? globalStrict : strict;
    }

    @Override
    public boolean authorize(String client, String resource) throws AuthException {
        if(resource == null) {
            throw new NullPointerException("resource is null");
        }
        if(client == null) {
            if(isStrict(resource)) {
                throw new AuthException("null is not authorized to visit " + resource);
            } else {
                return false;
            }
        }
        Set<String> allowedClients = resourceMap.get(resource);
        if(allowedClients != null && allowedClients.contains(client)) {
            return true;
        }
        if(isStrict(resource)) {
            throw new AuthException(client + " is not authorized to visit " + resource);
        }
        return false;
    }

}
