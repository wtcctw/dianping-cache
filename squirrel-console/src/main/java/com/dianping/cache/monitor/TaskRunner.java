package com.dianping.cache.monitor;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.util.IPUtils;

public class TaskRunner implements Runnable, ServerListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunner.class);
    
    private static final String NODE_MONITOR_KEY = "node.monitor.key";
    private static final int NODE_DEFAULT_EXPIRATION = 60; // seconds
    
    private final int LOG_THRESHOLD = 100;
    
    private final String LOCAL_IP = IPUtils.getFirstNoLoopbackIP4Address();

    private String server;
    
    private ServerState serverState;
    
    private long lastCheckTime = System.currentTimeMillis();

    private volatile boolean isStop = false;
    
    private CuratorFramework curatorClient;
    
    public TaskRunner(String server) {
        this.server = server;
        this.serverState = new ServerState(server);
        serverState.setServerListener(this);
        curatorClient = CuratorManager.getInstance().getCuratorClient();
    }
    
    @Override
    public void run() {
        if(!isStop) {
            doCheck();
        }
    }
    
    private void doCheck() {
        boolean alive = false;
        long start = System.currentTimeMillis();
        try {
            alive = checkNode();
        } catch (Throwable e) {
            alive = false;
            if(serverState.getDeadCount() % LOG_THRESHOLD == 0) {
                logger.error("failed to check server status: " + server, e);
            } else {
                logger.error("failed to check server status: " + server + ", error: " + e.getMessage());
            }
        } finally {
            lastCheckTime = System.currentTimeMillis();
            serverState.setAlive(alive);
            logger.info(serverState + ", time: " + (lastCheckTime-start));
        }
    }

    boolean checkNode() throws Exception {
        String value = RandomStringUtils.randomAlphanumeric(8);
        MemcachedClient mc = MemcachedClientFactory.getMemcachedClient(server);
        // go through the set & get circle
        mc.set(NODE_MONITOR_KEY, NODE_DEFAULT_EXPIRATION, value);
        String value2 = mc.get(NODE_MONITOR_KEY);
        return value.equals(value2);
    }

    public void pushServerStatus() {
        switch(serverState.getState()) {
        case Alive:
            try {
                markUp(server);
            } catch (Exception e) {
                logger.error("failed to mark up server " + server );
            }
            break;
        case Dead:
            try {
                markDown(server);
            } catch (Exception e) {
                logger.error("failed to mark down server " + server );
            }
            break;
        case Unknown:
            // do nothing
        }
    }
    
    @Override
    public void serverDead(String server) {
        try {
            markDown(server);
        } catch (Exception e) {
            logger.error("failed to mark down " + server, e);
        }
    }

    @Override
    public void serverAlive(String server) {
        try {
            markUp(server);
        } catch (Exception e) {
            logger.error("failed to mark up " + server, e);
        }
    }

    public void markDown(String memcached) throws Exception {
        String path = getMarkDownPath(memcached);
        // first delete then create to ensure the ZK node is owned by this session
        try {
            curatorClient.delete().forPath(path);
        } catch(NoNodeException e) {
        }
        try {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            logger.info("server " + memcached + " marked down");
        } catch(NodeExistsException e) {
            logger.warn("server " + memcached + " already marked down");
        }
    }

    public void markUp(String memcached) throws Exception {
        String path = getMarkDownPath(memcached);
        try {
            curatorClient.delete().forPath(path);
            logger.info("server " + memcached + " marked up");
        } catch(NoNodeException e) {
            logger.warn("server " + memcached + " already marked up");
        }
    }

    private String getMarkDownPath(String memcached) {
        StringBuilder buf = new StringBuilder(128);
        buf.append(Constants.MONITOR_PATH).append('/').append(memcached).append('/').append(LOCAL_IP);
        return buf.toString();
    }
    
    public ServerState getServerState() {
        return this.serverState;
    }
    
    public void setStop() {
        isStop = true;
    }
    
    public boolean isStop() {
        return isStop;
    }

}
