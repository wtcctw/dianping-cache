package com.dianping.cache.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class ServerState {

    public enum State {Alive, Dead, Unknown};
    
    private static final Logger logger = LoggerFactory.getLogger(ServerState.class);
    
    private ServerListener taskListener;
    
    private String server;
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private volatile int deadThreshold;

    private volatile int liveThreshold;
    
    private volatile boolean alive = false;
    
    private volatile int deadCount = 0;
    
    private volatile int liveCount = 0;
    
    private volatile State state = State.Unknown;
    
    private volatile State prevState = State.Unknown;
    
    public ServerState(String server) {
        this.server = server;
        deadThreshold = configManager.getIntValue(Constants.KEY_DEAD_THRESHOLD, Constants.DEFAULT_DEAD_THRESHOLD);
        liveThreshold = configManager.getIntValue(Constants.KEY_LIVE_THRESHOLD, Constants.DEFAULT_LIVE_THRESHOLD);
        try {
            configManager.registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(Constants.KEY_DEAD_THRESHOLD.equals(key)) {
                        deadThreshold = Integer.parseInt(value);
                    } else if(Constants.KEY_LIVE_THRESHOLD.equals(key)) {
                        liveThreshold = Integer.parseInt(value);
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
    }

    public void setAlive(boolean currentAlive) {
        boolean prevAlive = alive;
        alive = currentAlive;
        if(alive) {
            if(!prevAlive) {
                logger.info("server " + server + " status changed to alive");
                deadCount = 0;
                liveCount = 1;
            } else {
                if(++liveCount < 0) {
                    liveCount = liveThreshold + 1;
                }
            }
            if(liveCount == liveThreshold) {
                logger.warn("server " + server + " status confirmed to be alive");
                prevState = state;
                state = State.Alive;
                if(prevState == State.Dead) {
                    fireServerAlive();
                }
            }
        } else {
            if(prevAlive) {
                logger.warn("server " + server + " status changed to dead");
                liveCount = 0;
                deadCount = 1;
            } else {
                if(++deadCount < 0) {
                    deadCount = deadThreshold + 1;
                }
            }
            if(deadCount == deadThreshold) {
                logger.warn("server " + server + " status confirmed to be dead");
                prevState = state;
                state = State.Dead;
                if(prevState != State.Dead) {
                    fireServerDead();
                }
            }
        }
    }

    public boolean isAlive() {
        return state == State.Alive;
    }
    
    public boolean isDead() {
        return state == State.Dead;
    }
    
    public int getDeadCount() {
        return deadCount;
    }
    
    public int getLiveCount() {
        return liveCount;
    }
    
    
    public State getState() {
        return state;
    }
    
    private void fireServerDead() {
        if(taskListener != null) {
            try {
                taskListener.serverDead(server);
            } catch(Throwable t) {
                logger.error("failed to notify server dead", t);
            }
        }
    }

    private void fireServerAlive() {
        if(taskListener != null) {
            try {
                taskListener.serverAlive(server);
            } catch(Throwable t) {
                logger.error("failed to notify server alive", t);
            }
        }
    }

    public void setServerListener(ServerListener taskListener) {
        this.taskListener = taskListener;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("ServerState[server=").append(server).
                append(",dead=").append(deadCount).
                append(",live=").append(liveCount).append("]");
        return buf.toString();
    }
    
}
