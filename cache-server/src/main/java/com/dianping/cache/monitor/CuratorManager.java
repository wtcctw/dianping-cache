package com.dianping.cache.monitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

public class CuratorManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CuratorManager.class);

    private static class SingletonHolder {
        private static final CuratorManager INSTANCE = new CuratorManager();
    }
    
    public static CuratorManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private String zkAddress;
    
    private ThreadPoolExecutor eventThreadPool;
    
    private CuratorFramework curatorClient;
    
    private List<CuratorHandler> handlers;
    
    private CuratorManager() {
        try {
            init();
        } catch (Exception e) {
            logger.error("failed to initialize cache zookeeper: " + zkAddress, e);
        }
    };
    
    private void init() throws Exception {
        zkAddress = configManager.getStringValue(Constants.KEY_ZOOKEEPER_ADDRESS);
        if (StringUtils.isBlank(zkAddress))
            throw new NullPointerException("cache zookeeper address is empty");
        eventThreadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, 
                new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("cache-monitor-event", true));
        handlers = new CopyOnWriteArrayList<CuratorHandler>();
        curatorClient = newCuratorClient();
    }

    private CuratorFramework newCuratorClient() throws Exception {
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(zkAddress, 60 * 1000, 30 * 1000, 
                new RetryNTimes(3, 1000));
        
        curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                logger.info("zookeeper " + zkAddress + " state: " + newState);
                if (newState == ConnectionState.RECONNECTED) {
                    fireReconnected(client);
                }
            }

        }, eventThreadPool);
        
        curatorClient.getCuratorListenable().addListener(new CuratorListener() {

            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                fireEventReceived(client, event);
            }
            
        }, eventThreadPool);
        
        curatorClient.start();

        if (!curatorClient.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
            logger.error("timed out connecting to zookeeper: " + zkAddress);
        }
        
        return curatorClient;
    }

    public String getZkAddress() {
        return zkAddress;
    }
    
    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }
    
    public boolean isConnected() {
        try {
            return curatorClient.getZookeeperClient().getZooKeeper().getState().isConnected();
        } catch (Exception e) {
            logger.error("cache zookeeper is not connected", e);
            return false;
        }
    }
    
    public void ensurePath(String path) {
        try {
            curatorClient.create().creatingParentsIfNeeded().forPath(path);
        } catch(NodeExistsException e) {
        } catch(Exception e) {
            logger.error("failed to ensure path: " + path);
        }
    }
    
    public void deletePath(String path) throws Exception {
        try {
            curatorClient.delete().forPath(path);
        } catch(NoNodeException e) {
        } catch(Exception e) {
            logger.error("failed to delete path: " + path);
        }
    }
    
    protected void fireReconnected(CuratorFramework client) {
        for(CuratorHandler handler : handlers) {
            try {
                handler.reconnected();
            } catch(Throwable t) {
                logger.error("failed to invoke curator handler on reconnect", t);
            }
        }
    }
    
    protected void fireEventReceived(CuratorFramework client, CuratorEvent event) {
        if(event == null) {
            return;
        }
        WatchedEvent we = event.getWatchedEvent();
        if(we == null) {
            return;
        }
        if(we.getPath() == null) {
            return;
        }
        logger.info("received monitor event: " + we.getType() + " => " + we.getPath());
        for(CuratorHandler handler : handlers) {
            try {
                handler.eventReceived(we);
            } catch(Throwable t) {
                logger.error("failed to invoke curator handler on event: " + we, t);
            }
        }
    }
    
    public void addHandler(CuratorHandler handler) {
        if(!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }
    
}
