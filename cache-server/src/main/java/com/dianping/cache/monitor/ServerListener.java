package com.dianping.cache.monitor;

public interface ServerListener {

    public void serverDead(String server);
    
    public void serverAlive(String server);
    
}
