package com.dianping.cache.monitor2;

import com.dianping.cache.monitor.*;
import com.dianping.cache.monitor2.ServerListener;
import com.dianping.cache.monitor2.ServerState;
import com.dianping.squirrel.client.util.IPUtils;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.util.IPUtils;

public class TaskRunner implements Runnable, ServerListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private static final String NODE_MONITOR_KEY = "node.monitor.key";
    private static final int NODE_DEFAULT_EXPIRATION = 60; // seconds

    private final int LOG_THRESHOLD = 100;

    private final String LOCAL_IP = IPUtils.getFirstNoLoopbackIP4Address();

    private String server;

    private com.dianping.cache.monitor2.ServerState serverState;

    private long lastCheckTime = System.currentTimeMillis();

    private volatile boolean isStop = false;

    private CuratorFramework curatorClient;

    private ServerState.State preState = ServerState.State.Unknown;

    public TaskRunner(String server) {
        this.server = server;
        this.serverState = new com.dianping.cache.monitor2.ServerState(server);
        serverState.setServerListener(this);
        curatorClient = CuratorManager.getInstance().getCuratorClient();
    }

    @Override
    public void run() {
        try {
            doCheck();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doCheck() throws Exception {
        boolean alive = false;
        long start = System.currentTimeMillis();
        try {
            alive = checkNode();
        } catch (Throwable e) {
            alive = false;
            logger.error(server + " checked to be died");
        } finally {
            this.serverState.setAlive(alive, this);
            lastCheckTime = System.currentTimeMillis();
            logger.info(serverState + ", time: " + (lastCheckTime-start));
        }

    }

    boolean checkNode() throws Exception {
        int a = 1;
        String value = RandomStringUtils.randomAlphanumeric(8);
        MemcachedClient mc = MemcachedClientFactory.getInstance().getClient(server);
        // go through the set & get circle
        OperationFuture<Boolean> future = mc.set(NODE_MONITOR_KEY, NODE_DEFAULT_EXPIRATION, value);
        if(!future.get())
            return false;
        String value2 = (String) mc.get(NODE_MONITOR_KEY);
        return value.equals(value2);

    }

    @Override
    public void serverDead() {
        try {
            if(preState != ServerState.State.Dead) {
                preState = ServerState.State.Dead;
                NotifyManager.getInstance().notify("offline " + server, "offline " + server);
            }
            logger.info("server is mark down " + server);
            markDown(server);
        } catch (Exception e) {
            logger.error("failed to mark down " + server, e);
        }
    }

    @Override
    public void serverAlive() {
        try {
            if(preState != ServerState.State.Alive) {
                preState = ServerState.State.Alive;
                NotifyManager.getInstance().notify("online " + server, "online " + server);
            }
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
        buf.append(Constants.MARKDOWN_PATH).append('/').append(memcached).append('/').append(LOCAL_IP);
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
