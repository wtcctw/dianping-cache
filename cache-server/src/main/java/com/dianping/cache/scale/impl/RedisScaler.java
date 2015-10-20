package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import com.dianping.cache.scale.Node;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.ScalePlan;
import com.dianping.cache.scale.Scaler;

public class RedisScaler implements Scaler<RedisScalePlan>{

    private List<String> serverList;
    private List<String> addServerList;
    private RedisCluster cluster;
    
    public RedisScaler(List<String> serverList, List<String> addServerList) {
        this.serverList = serverList;
        this.addServerList = addServerList;
    }

    @Override
    public void scale() throws ScaleException {
        cluster = new RedisCluster(serverList);
        cluster.loadClusterInfo();
        RedisScalePlan scalePlan = new RedisScalePlan(cluster);
        List<RedisNode> addNodes = getAddNodes(addServerList);
        scalePlan.addNodes(addNodes);
        execute(scalePlan);
    }
    
    /*
     * 1 node consists of 2 servers. 
     * The first server is master.
     * The second server is slave. 
     */
    private List<RedisNode> getAddNodes(List<String> addServerList) {
        List<RedisNode> nodes = new ArrayList<RedisNode>();
        for(int i=0; i<addServerList.size()/2*2; i+=2) {
            RedisServer master = new RedisServer(addServerList.get(i));
            RedisServer slave = new RedisServer(addServerList.get(i+1));
            RedisNode node = new RedisNode(master, slave);
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public void execute(RedisScalePlan scalePlan) throws ScaleException {
        greet(scalePlan.getAddedNodes());
        for(ReshardRecord rr : scalePlan.reshardPlan()) {
            reshard(rr);
        }
    }

    /*
     * Add node to cluster and setup master/slave relation
     */
    public void greet(List<RedisNode> addedNodes) throws ScaleException {
        for(RedisNode node : addedNodes) {
            meet(node);
            if(node.getSlave() != null) {
                replicate(node);
            }
        }
    }

    void meet(RedisNode node) throws ScaleException {
        meet(node.getMaster());
        if(node.getSlave() != null) {
            meet(node.getSlave());
        }
    }

    void meet(RedisServer server) throws ScaleException {
        RedisNode node = cluster.getOneAliveNode();
        if(node == null) {
            throw new ScaleException(server + " failed to join cluster: no server alive");
        }
        Jedis jedis = RedisConnectionFactory.getConnection(server.getAddress());
        String status = jedis.clusterMeet(node.getMaster().getIp(), node.getMaster().getPort());
        if(!"OK".equals(status)) {
            throw new ScaleException(server + " failed to join cluster: " + status);
        }
    }

    boolean replicate(RedisNode node) {
        RedisServer slave = node.getSlave();
        Jedis jedis = RedisConnectionFactory.getConnection(slave.getAddress());
        String result = jedis.clusterReplicate(node.getMaster().getId());
        return "OK".equals(result);
    }
    
    void reshard(ReshardRecord rr) {
        RedisNode from = rr.getSourceNode();
        RedisNode to = rr.getDestNode();
        List<Integer> slots = rr.getSlots();
        for(int slot : slots) {
            migrate(from, to, slot);
        }
    }

    void migrate(RedisNode from, RedisNode to, int slot) {
        
    }
    
}
