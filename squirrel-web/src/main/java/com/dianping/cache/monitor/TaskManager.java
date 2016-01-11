package com.dianping.cache.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
import com.dianping.cache.util.CollectionUtils;
import com.dianping.cache.util.SpringLocator;
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
    
    private ConcurrentMap<String, Set<String>> serverClustersMap = new ConcurrentHashMap<String, Set<String>>();
    
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
        MemcachedClientFactory.getInstance().close();
    }
    
    private void addServerToCluster(String server, String cluster) {
        Set<String> clusters = serverClustersMap.get(server);
        if(clusters == null) {
            clusters = new HashSet<String>();
            serverClustersMap.put(server, clusters);
        }
        if(!clusters.contains(server)) {
            if(clusters.add(cluster)) {
                logger.info("added server " + server + " to cluster " + cluster);
            }
            logger.info("server " + server + " is now in clusters: " + listToString(clusters));
        }
        if(!serverTaskMap.containsKey(server)) {
            TaskRunner taskRunner = new TaskRunner(server);
            TaskRunner prevTask = serverTaskMap.put(server, taskRunner);
            logger.info("generated server task: " + server);
            if(prevTask != null) {
                prevTask.setStop();
                logger.info("stopped previous server task: " + server);
            }
            ScheduledFuture future = monitorThreadPool.scheduleWithFixedDelay(taskRunner, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
            ScheduledFuture prevFuture = serverFutureMap.put(server, future);
            logger.info("generated server task future: " + server);
            if(prevFuture != null) {
                if(prevFuture.cancel(false)) {
                    logger.info("cancelled previous server task future: " + server);
                }
            }
        }
    }

    private void removeServerFromCluster(String server, String cluster) {
        Set<String> clusters = serverClustersMap.get(server);
        if(clusters != null) {
            if(clusters.remove(server)) {
                logger.info("removed server " + server + " from cluster " + cluster);
            }
            logger.info("server " + server + " is now in clusters: " + listToString(clusters));
            if(clusters.isEmpty()) {
                serverClustersMap.remove(server);
            }
        }
        if(clusters == null || clusters.isEmpty()) {
            TaskRunner task = serverTaskMap.remove(server);
            if(task != null) {
                task.setStop();
                logger.info("stopped server task: " + server);
            }
            ScheduledFuture future = serverFutureMap.remove(server);
            if(future != null) {
                if(future.cancel(false)) {
                    logger.info("cancelled server task future: " + server);
                }
            }
        }
    }
    
    private boolean isMonitorable(CacheConfiguration cacheConfig) {
        String service = cacheConfig.getCacheKey();
        if(service == null) 
            return false;
        return service.startsWith("memcache");
    }
    
    @Override
    public void serviceAdded(CacheConfiguration cacheConfig) {
        if(isMonitorable(cacheConfig)) {
            CacheConfiguration oldCacheConfig = clusterConfigMap.put(cacheConfig.getCacheKey(), cacheConfig);
            _serviceChanged(cacheConfig, oldCacheConfig);
        } else {
            logger.info("service is not monitorable: " + cacheConfig.getCacheKey());
        }
    }
    
    @Override
    public void serviceRemoved(CacheConfiguration cacheConfig) {
        CacheConfiguration oldCacheConfig = clusterConfigMap.remove(cacheConfig.getCacheKey());
        _serviceChanged(null, oldCacheConfig);
    }
    
    @Override
    public void serviceChanged(CacheConfiguration cacheConfig) {
        if(isMonitorable(cacheConfig)) {
            CacheConfiguration oldCacheConfig = clusterConfigMap.put(cacheConfig.getCacheKey(), cacheConfig);
            logger.info("service changed, old: " + oldCacheConfig + ", new: " + cacheConfig);
            _serviceChanged(cacheConfig, oldCacheConfig);
        } else {
            logger.info("service is not monitorable: " + cacheConfig);
        }
    }
    
    private void _serviceChanged(CacheConfiguration newCacheConfig, CacheConfiguration oldCacheConfig) {
        List<String> oldServerList = oldCacheConfig == null ? null : oldCacheConfig.getServerList();
        List<String> newServerList = newCacheConfig == null ? null : newCacheConfig.getServerList();
        if(newServerList != null) {
            Collection<String> addedServers = CollectionUtils.subtract(newServerList, oldServerList);
            logger.info(newCacheConfig.getCacheKey() + " added servers: " + listToString(addedServers));
            for(String server : addedServers) {
                addServerToCluster(server, newCacheConfig.getCacheKey());
            }
        }
        if(oldServerList != null) {
            Collection<String> removedServers = CollectionUtils.subtract(oldServerList, newServerList);
            logger.info(newCacheConfig.getCacheKey() + " removed servers: " + listToString(removedServers));
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
    
    public Set<String> getClusters(String server) {
        return serverClustersMap.get(server);
    }
    
    public void offline(String server) {
        Set<String> clusters = serverClustersMap.get(server);
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
    
    private static String listToString(Collection<String> stringList) {
        if(stringList == null || stringList.size() == 0)
            return "";
        StringBuilder buf = new StringBuilder();
        for(String str : stringList) {
            buf.append(str).append(',');
        }
        return buf.substring(0, buf.length()-1);
    }
    
}
