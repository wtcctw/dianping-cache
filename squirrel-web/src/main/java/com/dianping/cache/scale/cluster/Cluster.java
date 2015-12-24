package com.dianping.cache.scale.cluster;

import java.util.List;

public interface Cluster<T extends Node> {

    public List<T> getNodes();
    
}
