package com.dianping.cache.scale;

import java.util.List;

public interface ScalePlan<T extends Node> {

    void addNode(T node) throws ScaleException;
    
    void addNodes(List<T> nodes) throws ScaleException;
    
    void removeNode(T node) throws ScaleException;
    
}
