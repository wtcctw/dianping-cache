package com.dianping.cache.monitor;

import org.apache.zookeeper.WatchedEvent;

public interface CuratorHandler {

    public void reconnected();
    
    public void eventReceived(WatchedEvent watchedEvent);
    
}
