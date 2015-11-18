package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.dianping.cache.scale.Cluster;
import com.dianping.cache.scale.ScaleException;

public class RedisCluster implements Cluster<RedisNode> {
    
    private static Logger logger = LoggerFactory.getLogger(RedisCluster.class);

    private List<String> serverList;
    
    private List<RedisNode> nodes;

    public RedisCluster(List<String> serverList) {
        this.serverList = serverList;
    }

    public void loadClusterInfo() throws ScaleException {
        if(serverList == null || serverList.size() == 0) {
            throw new ScaleException("server list is empty");
        }
        //目前没有考虑集群割裂的情况   仅仅是考虑有一个节点可能链接不上
        for(String address : serverList){
        	Jedis jedis;
			try {
				jedis = RedisConnectionFactory.getConnection(address);
				String clusterInfo = jedis.clusterNodes();
				nodes = parseClusterInfo(clusterInfo);
				break;
			} catch (Exception e) {
				RedisConnectionFactory.removeConnection(address);
				nodes = new ArrayList<RedisNode>();
				continue;
			}
        }
    }
    
    private List<RedisNode> parseClusterInfo(String clusterInfo) {
        List<RedisServer> servers = new ArrayList<RedisServer>();
        for(String serverInfo : clusterInfo.split("\n")) {
            RedisServer server = parseServerInfo(serverInfo);
            servers.add(server);
        }
        
        Map<String, RedisNode> nodeMap = new HashMap<String, RedisNode>();
        for(RedisServer server : servers) {
            if(server.isMaster()) {
                RedisNode node = new RedisNode();
                node.setMaster(server);
                nodeMap.put(server.getId(), node);
            }
        }
        for(RedisServer server : servers) {
            if(server.isSlave()  && !server.isFail()) {
                RedisNode node = nodeMap.get(server.getMasterId());
                if(node != null) {
                    node.setSlave(server);
                } else {
                    logger.warn("failed to find master for " + server);
                }
            }
        }
        return new ArrayList<RedisNode>(nodeMap.values());
    }

    private RedisServer parseServerInfo(String serverInfo) {
        String[] parts = serverInfo.split(" ");
        RedisServer server = new RedisServer(parts[1]);
        server.setId(parts[0]);
        server.setFlags(parts[2]);
        if(server.isMaster()) {
            String[] slots = Arrays.copyOfRange(parts, 8, parts.length);
            server.setSlots(slots);
        }
        if(server.isSlave()) {
            server.setMasterId(parts[3]);
        }
        return server;
    }

    @Override
    public List<RedisNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }
    
    public RedisNode getOneAliveNode() {
        for(RedisNode node : nodes) {
            RedisServer server = node.getMaster();
            if(server != null && server.isAlive()) {
                return node;
            }
        }
        return null;
    }
    
    public List<RedisNode> getAllAliveNodes() {
        List<RedisNode> aliveNodes = new ArrayList<RedisNode>();
        for(RedisNode node : nodes) {
            RedisServer master = node.getMaster();
            if(master != null && master.isAlive()) {
                aliveNodes.add(node);
            }
        }
        return aliveNodes;
    }
    
    public void checkSlotsCoverage() throws ScaleException {
        List<Integer> allSlots = new ArrayList<Integer>();
        for(RedisNode node : nodes) {
            RedisServer master = node.getMaster();
            if(master != null && master.isAlive() && master.getSlotList()!=null) {
                allSlots.addAll(master.getSlotList());
            }
        }
        if(allSlots.size() != 16384) {
            throw new ScaleException("not all slots covered");
        }
    }
    
}
