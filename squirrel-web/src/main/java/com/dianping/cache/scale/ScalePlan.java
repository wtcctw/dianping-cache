package com.dianping.cache.scale;

import java.util.List;

public interface ScalePlan<T extends Node> {

    public void addNode(T node) throws ScaleException;
    
    public void addNodes(List<T> nodes) throws ScaleException;
    
    public void removeNode(T node) throws ScaleException;
    
}
