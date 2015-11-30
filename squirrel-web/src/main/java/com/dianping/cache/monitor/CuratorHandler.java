package com.dianping.cache.monitor;

import org.apache.zookeeper.WatchedEvent;

public interface CuratorHandler {

    void reconnected();
    
    void eventReceived(WatchedEvent watchedEvent);
    
}
