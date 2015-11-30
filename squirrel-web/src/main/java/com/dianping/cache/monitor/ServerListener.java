package com.dianping.cache.monitor;

public interface ServerListener {

    void serverDead(String server);
    
    void serverAlive(String server);
    
}
