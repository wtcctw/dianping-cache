package com.dianping.squirrel.client.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.NamedThreadFactory;

public class DefaultAuthProvider implements AuthProvider {
    
    private static Logger logger = LoggerFactory.getLogger(DefaultAuthProvider.class);
    
    private static final String KEY_ZOOKEEPER_ADDRESS = "avatar-cache.zookeeper.address";
    
    private static final String AUTH_ROOT = "/dp/cache/auth";
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private CuratorFramework zkClient;

    private boolean globalStrict;
    private Map<String, Boolean> strictMap;
    private Map<String, Set<String>> resourceMap;
    
    public DefaultAuthProvider() throws Exception {
        String zkAddress = configManager.getStringValue(KEY_ZOOKEEPER_ADDRESS);
        if (StringUtils.isBlank(zkAddress))
            throw new NullPointerException("squirrel zookeeper address is empty");
        zkClient = CuratorFrameworkFactory.newClient(zkAddress, 60 * 1000, 30 * 1000, 
                new RetryNTimes(3, 1000));
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
        String value = getData(AUTH_ROOT);
        return value != null && value.equalsIgnoreCase("true");
    }

    private String getData(String path) throws Exception {
        try {
            byte[] bytes = zkClient.getData().forPath(path);
            return bytes == null ? null : new String(bytes);
        } catch(NoNodeException e) {
            return null;
        }
    }

    private Map<String, Boolean> loadStrictMap() throws Exception {
        Map<String, Boolean> strictMap = new HashMap<String, Boolean>();
        List<String> children = getChildren(AUTH_ROOT);
        for(String child : children) {
            String path = AUTH_ROOT + "/" + child;
            String data = getData(path);
            if(data != null) {
                boolean strict = data.equalsIgnoreCase("true");
                strictMap.put(child, strict);
            }
        }
        return strictMap;
    }
    
    private List<String> getChildren(String path) throws Exception {
        try {
            List<String> children = zkClient.getChildren().forPath(path);
            return children;
        } catch(NoNodeException e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Set<String>> loadResourceMap() throws Exception {
        Map<String, Set<String>> resourceMap = new HashMap<String, Set<String>>();
        List<String> children = getChildren(AUTH_ROOT);
        for(String child : children) {
            String path = AUTH_ROOT + "/" + child + "/applications";
            String data = getData(path);
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
