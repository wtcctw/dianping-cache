package com.dianping.cache.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.util.CollectionUtils;
import com.dianping.cat.Cat;
import com.dianping.lion.Environment;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class TaskMonitor implements CuratorHandler {

    private static final Logger logger = LoggerFactory.getLogger(TaskMonitor.class);

    private enum State {
        Alive, Dead, Unsure
    }

    private TaskManager taskManager;

    private MemberMonitor memberMonitor;

    private CuratorFramework curatorClient;

    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private volatile ConcurrentMap<String, List<String>> serverStatus = new ConcurrentHashMap<String, List<String>>();

    private volatile ConcurrentMap<String, State> serverStates = new ConcurrentHashMap<String, State>();

    private volatile int memberMinimum;

    private volatile float memberPercent;

    private volatile boolean enableOffline;

    private volatile boolean enableOnline;

    TaskMonitor(TaskManager taskManager) {
        this.taskManager = taskManager;
        memberMonitor = new MemberMonitor();
        CuratorManager.getInstance().ensurePath(Constants.MONITOR_PATH);
        CuratorManager.getInstance().addHandler(this);
        curatorClient = CuratorManager.getInstance().getCuratorClient();
        memberMinimum = configManager.getIntValue(Constants.KEY_MONITOR_MEMBER_MINIMUM,
                Constants.DEFAULT_MONITOR_MEMBER_MINIMUM);
        memberPercent = configManager.getFloatValue(Constants.KEY_MONITOR_MEMBER_PERCENT,
                Constants.DEFAULT_MONITOR_MEMBER_PERCENT);
        enableOffline = configManager.getBooleanValue(Constants.KEY_MONITOR_ENABLE_OFFLINE,
                Constants.DEFAULT_MONITOR_ENABLE_OFFLINE);
        enableOnline = configManager.getBooleanValue(Constants.KEY_MONITOR_ENABLE_ONLINE,
                Constants.DEFAULT_MONITOR_ENABLE_ONLINE);
        try {
            configManager.registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if (Constants.KEY_MONITOR_ENABLE_OFFLINE.equals(key)) {
                        enableOffline = Boolean.parseBoolean(value);
                    } else if (Constants.KEY_MONITOR_ENABLE_ONLINE.equals(key)) {
                        enableOnline = Boolean.parseBoolean(value);
                    } else if (Constants.KEY_MONITOR_MEMBER_PERCENT.equals(key)) {
                        memberPercent = Float.parseFloat(value);
                    } else if (Constants.KEY_MONITOR_MEMBER_MINIMUM.equals(key)) {
                        memberMinimum = Integer.parseInt(value);
                    }
                }

            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }

        refreshServerStatus();
    }

    private void refreshServerStatus() {
        try {
            pushServerStatus();
            pollServerStatus();
        } catch (Exception e) {
            logger.error("failed to refresh server status", e);
        }
    }

    private void pushServerStatus() {
        if (taskManager != null) {
            for (String server : taskManager.getServers()) {
                TaskRunner taskRunner = taskManager.getTaskRunner(server);
                if (taskRunner != null) {
                    taskRunner.pushServerStatus();
                }
            }
        }
    }

    private void pollServerStatus() throws Exception {
        List<String> servers = curatorClient.getChildren().watched().forPath(Constants.MONITOR_PATH);

        for (String server : servers) {
            try {
                List<String> status = getServerStatus(server);
                if (status != null) {
                    serverStatus.put(server, status);
                    State state = getServerState(status);
                    State prevState = this.serverStates.get(server);
                    checkStateChange(server, prevState, state, status);
                }
            } catch (Exception e) {
                logger.error("failed to get server status: " + server, e);
            }
        }
    }

    private void checkStateChange(String server, State prevState, State state, List<String> status) {
        if (state == null) {
            logger.warn("server state is null: " + server);
            return;
        }
        switch (state) {
        case Alive:
            if (prevState == State.Dead) {
                logger.info("effective server status change: " + server + ", monitors: "
                        + CollectionUtils.toString(status));
                fireServerAlive(server, status);
            }
            break;
        case Dead:
            if (prevState != State.Dead) {
                logger.info("effective server status change: " + server + ", monitors: "
                        + CollectionUtils.toString(status));
                fireServerDead(server, status);
            }
            break;
        case Unsure:
            // do nothing
            break;
        }
    }

    private List<String> getServerStatus(String server) throws Exception {
        String path = Constants.MONITOR_PATH + "/" + server;
        List<String> status = curatorClient.getChildren().watched().forPath(path);
        return status;
    }

    @Override
    public void reconnected() {
        refreshServerStatus();
    }

    @Override
    public void eventReceived(WatchedEvent we) {
        String path = we.getPath();
        if (!path.startsWith(Constants.MONITOR_PATH)) {
            return;
        }
        if (Constants.MONITOR_PATH.equals(path) && EventType.NodeChildrenChanged == we.getType()) {
            // server list changed
            try {
                Collection<String> oldServers = serverStatus.keySet();
                List<String> newServers = curatorClient.getChildren().watched().forPath(Constants.MONITOR_PATH);
                Collection<String> addedServers = CollectionUtils.subtract(newServers, oldServers);
                for (String server : addedServers) {
                    List<String> status = getServerStatus(server);
                    if (status != null) {
                        serverStatus.put(server, status);
                        logger.info("server status added: " + server + ", monitors: "
                                + CollectionUtils.toString(status));
                        State state = getServerState(status);
                        State prevState = this.serverStates.get(server);
                        checkStateChange(server, prevState, state, status);
                    }
                }
                Collection<String> removedServers = CollectionUtils.subtract(oldServers, newServers);
                for (String server : removedServers) {
                    List<String> prevStatus = serverStatus.remove(server);
                    logger.info("server status removed: " + server + ", previous monitors: "
                            + CollectionUtils.toString(prevStatus));
                    State state = getServerState(Collections.EMPTY_LIST);
                    State prevState = this.serverStates.get(server);
                    checkStateChange(server, prevState, state, Collections.EMPTY_LIST);
                }
            } catch (Exception e) {
                logger.error("failed to update server status list", e);
            }
        }
        if (path.length() > Constants.MONITOR_PATH.length() + 1 && EventType.NodeChildrenChanged == we.getType()) {
            // server status changed
            String server = path.substring(Constants.MONITOR_PATH.length() + 1);
            try {
                List<String> status = getServerStatus(server);
                if (status != null) {
                    List<String> prevStatus = serverStatus.put(server, status);
                    logger.info("server status changed: " + server + ", previous monitors: "
                            + CollectionUtils.toString(prevStatus) + ", current monitors: "
                            + CollectionUtils.toString(status));
                    State state = getServerState(status);
                    State prevState = this.serverStates.get(server);
                    checkStateChange(server, prevState, state, status);
                }
            } catch (Exception e) {
                logger.error("failed to get server status: " + server, e);
            }
        }
    }

    private void fireServerDead(String server, List<String> status) {
        logger.info("server " + server + " is confirmed dead by " + CollectionUtils.toString(status));
        Cat.logEvent("Squirrel.monitor", server + ":dead");
        serverStates.put(server, State.Dead);
        if (memberMonitor.isMaster()) {
            notifyServerDead(server);
            if (enableOffline) {
                taskManager.offline(server);
            }
        }
    }

    private void fireServerAlive(String server, List<String> status) {
        logger.info("server " + server + " is confirmed alive");
        Cat.logEvent("Squirrel.monitor", server + ":alive");
        serverStates.put(server, State.Alive);
        if (memberMonitor.isMaster()) {
            notifyServerAlive(server);
            if (enableOnline) {
                taskManager.online(server);
            }
        }
    }

    private State getServerState(List<String> serverStatus) {
        int memberCount = memberMonitor.getMemberCount();
        if (memberCount >= memberMinimum) {
            int num = CollectionUtils.size(serverStatus);
            float upperBound = memberCount * memberPercent;
            if (num >= upperBound)
                return State.Dead;
            float lowerBound = memberCount * (1 - memberPercent);
            if (num <= lowerBound)
                return State.Alive;
        }
        return State.Unsure;
    }

    private boolean isServerDead(List<String> status) {
        int memberCount = memberMonitor.getMemberCount();
        if (memberCount >= memberMinimum) {
            if (CollectionUtils.size(status) >= memberCount * memberPercent) {
                return true;
            }
        }
        return false;
    }

    private boolean isServerAlive(List<String> status) {
        int memberCount = memberMonitor.getMemberCount();
        if (memberCount >= memberMinimum) {
            if (CollectionUtils.size(status) <= memberCount * (1 - memberPercent)) {
                return true;
            }
        }
        return false;
    }

    private void notifyServerDead(String server) {
        StringBuilder buf = new StringBuilder(256);
        buf.append("memcached server ").append(server);
        buf.append(" in env ").append(Environment.getEnv());
        buf.append(" in cluster ").append(CollectionUtils.toString(taskManager == null ? null : taskManager.getClusters(server)));
        buf.append(" is confirmed to be dead");
        NotifyManager.getInstance().notify("memcached server " + server + " is dead", buf.toString());
    }

    private void notifyServerAlive(String server) {
        StringBuilder buf = new StringBuilder(256);
        buf.append("memcached server ").append(server);
        buf.append(" in env ").append(Environment.getEnv());
        buf.append(" in cluster ").append(CollectionUtils.toString(taskManager == null ? null : taskManager.getClusters(server)));
        buf.append(" is confirmed to be alive");
        NotifyManager.getInstance().notify("memcached server " + server + " is alive", buf.toString());
    }

}
