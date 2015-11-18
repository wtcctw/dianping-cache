package com.dianping.cache.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.CollectionUtils;
import com.dianping.lion.Environment;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class TaskManager implements ServiceListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    
    private final int CORES = Runtime.getRuntime().availableProcessors();
    
    private ServiceMonitor serviceMonitor;
    
    private TaskMonitor taskMonitor;
    
    private ScheduledThreadPoolExecutor monitorThreadPool;
    
    private ConcurrentMap<String, List<String>> serverClustersMap = new ConcurrentHashMap<String, List<String>>();
    
    private ConcurrentMap<String, CacheConfiguration> clusterConfigMap = new ConcurrentHashMap<String, CacheConfiguration>();
    
    private ConcurrentMap<String, TaskRunner> serverTaskMap = new ConcurrentHashMap<String, TaskRunner>();

    private ConcurrentMap<String, ScheduledFuture> serverFutureMap = new ConcurrentHashMap<String, ScheduledFuture>();
    
    private CacheConfigurationService cacheConfigService;
    
    private ConfigManager configManager;
    
    private volatile boolean enableMonitor;
    
    private volatile int monitorInterval;
    
    private volatile int serverMinimum;
    
    private volatile float serverPercent;
    
    public TaskManager() {
        configManager = ConfigManagerLoader.getConfigManager();
        enableMonitor = configManager.getBooleanValue(
                Constants.KEY_MONITOR_ENABLE, Constants.DEFAULT_MONITOR_ENABLE);
        monitorInterval = configManager.getIntValue(
                Constants.KEY_MONITOR_INTERVAL, Constants.DEFAULT_MONITOR_INTERVAL);
        serverMinimum = configManager.getIntValue(
                Constants.KEY_MONITOR_SERVER_MINIMUM, Constants.DEFAULT_MONITOR_SERVER_MINIMUM);
        serverPercent = configManager.getFloatValue(
                Constants.KEY_MONITOR_SERVER_PERCENT, Constants.DEFAULT_MONITOR_SERVER_PERCENT);
        try {
            configManager.registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(Constants.KEY_MONITOR_ENABLE.equals(key)) {
                        enableMonitor = Boolean.parseBoolean(value);
                        if(!enableMonitor) {
                            if(monitorThreadPool != null) {
                                monitorThreadPool.shutdown();
                            }
                        }
                    } else if(Constants.KEY_MONITOR_INTERVAL.equals(key)) {
                        monitorInterval = Integer.parseInt(value);
                    } else if(Constants.KEY_MONITOR_SERVER_PERCENT.equals(key)) {
                        serverPercent = Float.parseFloat(value);
                    } else if(Constants.KEY_MONITOR_SERVER_MINIMUM.equals(key)) {
                        serverMinimum = Integer.parseInt(value);
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
        
        cacheConfigService = SpringLocator.getBean(CacheConfigurationService.class);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                enableMonitor = false;
                TaskManager.this.stop();
            }
        });
    }
    
    public void start() {
        if(enableMonitor) {
            logger.info("starting cache monitor...");
            monitorThreadPool = new ScheduledThreadPoolExecutor(CORES, new NamedThreadFactory("cache-monitor", true));

            serviceMonitor = new ServiceMonitor();
            serviceMonitor.addServiceListener(this);
            taskMonitor = new TaskMonitor(this);
            
            doStart();
            logger.info("cache monitor started");
        }
    }
    
    private void doStart() {
        Collection<CacheConfiguration> cacheConfigs = serviceMonitor.getServiceConfigs();
        if(cacheConfigs != null) { 
            for(CacheConfiguration cacheConfig : cacheConfigs) {
                if(isMonitorable(cacheConfig)) {
                    clusterConfigMap.put(cacheConfig.getCacheKey(), cacheConfig);
                    for(String server : cacheConfig.getServerList()) {
                        int idx = server.indexOf('|');
                        if(idx != -1) {
                            logger.warn("backup server is not supported: " + cacheConfig);
                            continue;
                        }
                        addServerToCluster(server, cacheConfig.getCacheKey());
                    }
                }
            }
        }
    }

    public void stop() {
        if(monitorThreadPool != null) {
            monitorThreadPool.shutdownNow();
        }
        MemcachedClientFactory.closeAll();
    }
    
    private void addServerToCluster(String server, String cluster) {
        List<String> clusters = serverClustersMap.get(server);
        if(clusters == null) {
            clusters = new ArrayList<String>();
            serverClustersMap.put(server, clusters);
        }
        if(!clusters.contains(server)) {
            clusters.add(cluster);
        }
        if(!serverTaskMap.containsKey(server)) {
            TaskRunner taskRunner = new TaskRunner(server);
            serverTaskMap.put(server, taskRunner);
            ScheduledFuture future = monitorThreadPool.scheduleWithFixedDelay(taskRunner, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
            serverFutureMap.put(server, future);
        }
    }

    private void removeServerFromCluster(String server, String cluster) {
        List<String> clusters = serverClustersMap.get(server);
        if(clusters != null) {
            clusters.remove(server);
            if(clusters.isEmpty()) {
                serverClustersMap.remove(server);
            }
        }
        if(clusters == null || clusters.isEmpty()) {
            TaskRunner task = serverTaskMap.remove(server);
            if(task != null) {
                task.setStop();
            }
            ScheduledFuture future = serverFutureMap.remove(server);
            if(future != null) {
                future.cancel(false);
            }
        }
    }
    
    private boolean isMonitorable(CacheConfiguration cacheConfig) {
        String service = cacheConfig.getCacheKey();
        if(service == null) 
            return false;
        if(service.equals("web") || service.startsWith("dcache") || 
           service.equals("kvdb") || service.startsWith("redis")) {
            return false;
        }
        return true;
    }
    
    @Override
    public void serviceAdded(CacheConfiguration cacheConfig) {
        CacheConfiguration oldCacheConfig = clusterConfigMap.put(cacheConfig.getCacheKey(), cacheConfig);
        _serviceChanged(cacheConfig, oldCacheConfig);
    }
    
    @Override
    public void serviceRemoved(CacheConfiguration cacheConfig) {
        CacheConfiguration oldCacheConfig = clusterConfigMap.remove(cacheConfig.getCacheKey());
        _serviceChanged(null, oldCacheConfig);
    }
    
    @Override
    public void serviceChanged(CacheConfiguration cacheConfig) {
        CacheConfiguration oldCacheConfig = clusterConfigMap.put(cacheConfig.getCacheKey(), cacheConfig);
        _serviceChanged(cacheConfig, oldCacheConfig);
    }
    
    private void _serviceChanged(CacheConfiguration newCacheConfig, CacheConfiguration oldCacheConfig) {
        List<String> oldServerList = oldCacheConfig == null ? null : oldCacheConfig.getServerList();
        List<String> newServerList = newCacheConfig == null ? null : newCacheConfig.getServerList();
        if(newServerList != null) {
            Collection<String> addedServers = CollectionUtils.subtract(newServerList, oldServerList);
            for(String server : addedServers) {
                addServerToCluster(server, newCacheConfig.getCacheKey());
            }
        }
        if(oldServerList != null) {
            Collection<String> removedServers = CollectionUtils.subtract(oldServerList, newServerList);
            for(String server : removedServers) {
                removeServerFromCluster(server, newCacheConfig.getCacheKey());
            }
        }
    }

    public Set<String> getServers() {
        return serverTaskMap.keySet();
    }
    
    public TaskRunner getTaskRunner(String server) {
        return server == null ? null : serverTaskMap.get(server);
    }
    
    public List<String> getClusters(String server) {
        return serverClustersMap.get(server);
    }
    
    public void offline(String server) {
        List<String> clusters = serverClustersMap.get(server);
        if(clusters != null) {
            for(String cluster : clusters) {
                CacheConfiguration cacheConfig = clusterConfigMap.get(cluster);
                if(canOfflineFromCluster(server, cacheConfig)) {
                    offlineFromCluster(server, cacheConfig);
                    String message = "offlined memcached server " + server + " from cluster " + cacheConfig.getCacheKey() + " in env " + Environment.getEnv();
                    logger.info(message);
                    NotifyManager.getInstance().notify("offlined memcached server " + server, message);
                } else {
                    String message = "memcached server " + server + " in cluster " + cacheConfig.getCacheKey() + " in env " + Environment.getEnv() + 
                            " doesn't meet offline criteria, will not offline memcached server " + server;
                    logger.info(message);
                    NotifyManager.getInstance().notify("Will not offline memcached server " + server, message);
                }
            }
        }
    }

    private boolean canOfflineFromCluster(String server, CacheConfiguration cacheConfig) {
        List<String> serverList = cacheConfig.getServerList();
        if(serverList != null && serverList.contains(server)) {
            return (serverList.size() - 1) >= (serverList.size() * serverPercent);
        }
        return false;
    }
    
    private void offlineFromCluster(String server, CacheConfiguration cacheConfig) {
        List<String> serverList = new ArrayList<String>(cacheConfig.getServerList());
        serverList.remove(server);
        CacheConfiguration updatedCacheConfig = new CacheConfiguration();
        updatedCacheConfig.setCacheKey(cacheConfig.getCacheKey());
        updatedCacheConfig.setClientClazz(cacheConfig.getClientClazz());
        updatedCacheConfig.setServerList(serverList);
        updatedCacheConfig.setTranscoderClazz(cacheConfig.getTranscoderClazz());
        cacheConfigService.update(updatedCacheConfig);
    }

    public void online(String server) {
        logger.info("online server " + server + ", operation not supported");
    }
    
}
