package com.dianping.cache.scale;

import java.util.List;

public interface Cluster<T extends Node> {

    List<T> getNodes();
    
}
