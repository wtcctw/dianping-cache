package com.dianping.cache.monitor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.util.CollectionUtils;
import com.dianping.squirrel.common.util.JsonUtils;

public class ServiceMonitor implements CuratorHandler {

    private static Logger logger = LoggerFactory.getLogger(ServiceMonitor.class);

    private final int STATE_SUCC = 1;
    
    private final int STATE_FAIL = -1;

    private CuratorFramework curatorClient;
    
    private ConcurrentMap<String, CacheConfiguration> serviceConfigs;
    
    private ServiceListener serviceListener;
    
    private Lock lock = new ReentrantLock();
    
    private int state = 0;
    
    ServiceMonitor() {
        CuratorManager.getInstance().ensurePath(Constants.SERVICE_PATH);
        CuratorManager.getInstance().addHandler(this);
        curatorClient = CuratorManager.getInstance().getCuratorClient();
        refreshServiceConfigs();
    }

    private void refreshServiceConfigs() {
        lock.lock();
        try {
            serviceConfigs = loadServiceConfigs();
            if(logger.isInfoEnabled()) {
                if(serviceConfigs != null) {
                    logger.info("loaded service configs:");
                    for(CacheConfiguration config : serviceConfigs.values()) {
                        logger.info(config.toString());
                    }
                }
            }
            state = STATE_SUCC;
        } catch (Exception e) {
            state = STATE_FAIL;
            logger.error("failed to load service configs", e);
        } finally {
            lock.unlock();
        }
    }
     
    public Collection<CacheConfiguration> getServiceConfigs() {
        lock.lock();
        try { 
            return state == STATE_SUCC ? serviceConfigs.values() : null;
        } finally {
            lock.unlock();
        }
    }
    
    private ConcurrentMap<String, CacheConfiguration> loadServiceConfigs() throws Exception {
        ConcurrentMap<String, CacheConfiguration> configs = new ConcurrentHashMap<String, CacheConfiguration>();
        List<String> services = curatorClient.getChildren().watched().forPath(Constants.SERVICE_PATH);
        for(String service : services) {
            try {
                CacheConfiguration config = loadServiceConfig(service);
                if(config == null) {
                    logger.error(service + "'s service config is empty");
                    continue;
                }
                configs.put(config.getCacheKey(), config);
            } catch (Exception e) {
                logger.error("failed to load service config: " + service, e);
            }
        }
        return configs;
    }
    
    private CacheConfiguration loadServiceConfig(String service) throws Exception {
        byte[] data = curatorClient.getData().watched().forPath(Constants.SERVICE_PATH + "/" + service);
        if(data == null || data.length==0) {
            return null;
        }
        String value = new String(data, "UTF-8");
        CacheConfiguration config = JsonUtils.fromStr(value, CacheConfiguration.class);
        return config;
    }

    public void addServiceListener(ServiceListener serviceListener) {
        this.serviceListener = serviceListener;
    }

    @Override
    public void reconnected() {
        refreshServiceConfigs();
    }

    @Override
    public void eventReceived(WatchedEvent we) {
        String path = we.getPath();
        if(!path.startsWith(Constants.SERVICE_PATH)) {
            return;
        }
        if(path.equals(Constants.SERVICE_PATH) && 
                we.getType() == EventType.NodeChildrenChanged) {
            // service added or removed
            try {
                Collection<String> oldServices = serviceConfigs.keySet();
                List<String> newServices = curatorClient.getChildren().watched().forPath(Constants.SERVICE_PATH);
                Collection<String> addedServices = CollectionUtils.subtract(newServices, oldServices);
                for(String service : addedServices) {
                    CacheConfiguration config = loadServiceConfig(service);
                    if(config != null) {
                        logger.info("service added: " + config);
                        serviceConfigs.put(config.getCacheKey(), config);
                        fireServiceAdded(config);
                    }
                }
                Collection<String> removedServices = CollectionUtils.subtract(oldServices, newServices);
                for(String service : removedServices) {
                    CacheConfiguration config = serviceConfigs.remove(service);
                    if(config != null) {
                        logger.info("service removed: " + config);
                        fireServiceRemoved(config);
                    }
                }
            } catch (Exception e) {
                logger.error("failed to load service configs", e);
            }
        } else if(path.length() > Constants.SERVICE_PATH.length() + 1 &&
                we.getType() == EventType.NodeDataChanged) {
            // service changed
            String service = path.substring(Constants.SERVICE_PATH.length() + 1);
            try {
                CacheConfiguration config = loadServiceConfig(service);
                if(config != null) {
                    logger.info("service changed: " + config);
                    serviceConfigs.put(config.getCacheKey(), config);
                    fireServiceChanged(config);
                }
            } catch (Exception e) {
                logger.error("failed to load service config: " + service, e);
            }
        }
    }

    private void fireServiceAdded(CacheConfiguration config) {
        if(serviceListener != null) {
            try {
                serviceListener.serviceAdded(config);
            } catch(Throwable t) {
                logger.error("failed to notify service add: " + config, t);
            }
        }
    }

    private void fireServiceRemoved(CacheConfiguration config) {
        if(serviceListener != null) {
            try {
                serviceListener.serviceRemoved(config);
            } catch(Throwable t) {
                logger.error("failed to notify service remove: " + config, t);
            }
        }
    }
    
    private void fireServiceChanged(CacheConfiguration config) {
        if(serviceListener != null) {
            try {
                serviceListener.serviceChanged(config);
            } catch(Throwable t) {
                logger.error("failed to notify service change: " + config, t);
            }
        }
    }

}
