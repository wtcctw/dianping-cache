package com.dianping.cache.monitor2;

import com.dianping.cache.monitor.Constants;
import com.dianping.cache.monitor2.ServerListener;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerState {

    public enum State {Alive, Dead, Unknown}

    private static final Logger logger = LoggerFactory.getLogger(ServerState.class);
    
    private ServerListener taskListener;
    
    private String server;
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public String getServer() {
        return server;
    }

    private volatile int deadThreshold;

    private volatile int liveThreshold;
    
    private volatile boolean alive = false;
    
    private volatile int deadCount = 0;
    
    private volatile int liveCount = 0;
    
    private volatile State state = State.Unknown;
    
    private volatile State prevNotifyState = State.Unknown;
    
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

    public void setAlive(boolean currentAlive, ServerListener listener) {
        alive = currentAlive;
        if(alive) {
            if(++liveCount % liveThreshold == 0) {
                listener.serverAlive();
                state = State.Alive;
            }
            deadCount = 0;
        } else {
            if(++deadCount % deadThreshold == 0) {
                listener.serverDead();
                state = State.Dead;
            }
            liveCount = 0;
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
