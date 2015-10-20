package com.dianping.cache.scale.impl;

import java.util.List;


public class ReshardRecord {

    private RedisNode sourceNode;
    
    private RedisNode destNode;
    
    private List<Integer> slots;

    public RedisNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(RedisNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public RedisNode getDestNode() {
        return destNode;
    }

    public void setDestNode(RedisNode destNode) {
        this.destNode = destNode;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlot(List<Integer> slots) {
        this.slots = slots;
    }
    
}
