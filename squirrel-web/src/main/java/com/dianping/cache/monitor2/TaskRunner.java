package com.dianping.cache.monitor2;

import com.dianping.cache.monitor.*;
import com.dianping.squirrel.client.util.IPUtils;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunder on 16/1/29.
 */
public class TaskRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private static final String NODE_MONITOR_KEY = "node.monitor.key";

    private static final int NODE_DEFAULT_EXPIRATION = 60; // seconds

    private final int LOG_THRESHOLD = 100;

    private String LOCAL_IP = IPUtils.getFirstNoLoopbackIP4Address();

    private String server;

    private ServerState serverState;

    private long lastCheckTime = System.currentTimeMillis();

    private volatile boolean isStop = false;

    private CuratorFramework curatorClient;

    public TaskRunner(ServerState state) {
        this.server = state.getServer();
        this.serverState = state;
        curatorClient = CuratorManager.getInstance().getCuratorClient();
    }


    @Override
    public void run() {
        doCheck();
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
        MemcachedClient mc = MemcachedClientFactory.getInstance().getClient(server);
        // go through the set & get circle
        OperationFuture<Boolean> future = mc.set(NODE_MONITOR_KEY, NODE_DEFAULT_EXPIRATION, value);
        if(!future.get())
            return false;
        String value2 = (String) mc.get(NODE_MONITOR_KEY);
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

    public void markDown(String memcached) throws Exception {
        String path = getMarkDownPath(memcached);
        // first delete then create to ensure the ZK node is owned by this session
        try {
            curatorClient.delete().forPath(path);
            curatorClient.delete().forPath(getMarkUpPath(memcached));
        } catch(KeeperException.NoNodeException e) {
        }
        try {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            logger.info("server " + memcached + " marked down");
        } catch(KeeperException.NodeExistsException e) {
            logger.warn("server " + memcached + " already marked down");
        }
    }

    public void markUp(String memcached) throws Exception {

        String path = getMarkUpPath(memcached);

        // first delete then create to ensure the ZK node is owned by this session
        try {
            curatorClient.delete().forPath(path);
            curatorClient.delete().forPath(getMarkDownPath(path));
        } catch(KeeperException.NoNodeException e) {
        }
        try {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            logger.info("server " + memcached + " marked up");
        } catch(KeeperException.NodeExistsException e) {
            logger.warn("server " + memcached + " already marked up");
        }
    }

    private String getMarkUpPath(String memcached) {
        StringBuilder buf = new StringBuilder(128);
        buf.append(Constants.MONITOR_MARKUP_PATH).append('/').append(memcached).append('/').append(LOCAL_IP);
        return buf.toString();
    }

    private String getMarkDownPath(String memcached) {
        StringBuilder buf = new StringBuilder(128);
        buf.append(Constants.MONITOR_MARKDOWN_PATH).append('/').append(memcached).append('/').append(LOCAL_IP);
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
