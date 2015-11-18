package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;

import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.ScalePlan;
import com.qq.cloud.component.jce.client.util.StringUtils;

public class RedisScalePlan implements ScalePlan<RedisNode> {

    private List<RedisNode> addedNodes;
    
    private List<RedisNode> deledNodes;
    
    private RedisCluster cluster;
    
    public RedisScalePlan(RedisCluster cluster) {
        this.cluster = cluster;
    }
    
    public void addNode(RedisNode node) throws ScaleException {
        if(node == null) {
            throw new NullPointerException("node is null");
        }
        //validate(node);
        if(addedNodes == null) {
            addedNodes = new ArrayList<RedisNode>();
        }
        if(!addedNodes.contains(node)) {
            addedNodes.add(node);
        }
    }
    
    public void addNodes(List<RedisNode> nodes) throws ScaleException {
        for(RedisNode node : nodes) {
            addNode(node);
        }
    }
    
    public List<RedisNode> getAddedNodes() {
        return addedNodes;
    }

    void validate(RedisNode node) throws ScaleException {
        RedisServer server = node.getMaster();
        if(server == null) {
            throw new ScaleException("master is null");
        }
        validate(server);
        server = node.getSlave();
        if(server != null) {
            validate(server);
        }
    }
    
    void validate(RedisServer server) throws ScaleException {
        if(!isClusterEnabled(server.getAddress())) {
            throw new ScaleException("cluster is not enabled: " + server);
        }
        if(!isSingleEmptyCluster(server)) {
            throw new ScaleException("not single empty cluster: " + server);
        }
    }
    
    public static boolean isSingleEmptyCluster(RedisServer server) throws ScaleException {
        List<String> serverList = new ArrayList<String>();
        serverList.add(server.getAddress());
        RedisCluster cluster = new RedisCluster(serverList);
        cluster.loadClusterInfo();
        List<RedisNode> nodes = cluster.getNodes();
        if(nodes.size() == 1) {
            RedisNode node = nodes.get(0);
            if(node.getMaster() != null && node.getSlave() == null) {
                RedisServer rs = node.getMaster();
                if(rs.isMyself() && StringUtils.isEmpty(rs.getSlotString())) {
                    server.setId(rs.getId());
                    return true;
                }
            }
        }
        return false;
    }
    
    /*
     * info cluster: cluster_enabled:1
     */
    public static boolean isClusterEnabled(String server) {
        Jedis jedis = RedisConnectionFactory.getConnection(server);
        String clusterInfo = jedis.info("cluster");
        return clusterInfo == null ? false : clusterInfo.contains("cluster_enabled:1");
    }
    
    public List<ReshardRecord> reshardPlan() throws ScaleException {
        cluster.checkSlotsCoverage();
        List<ReshardRecord> reshardRecord = new ArrayList<ReshardRecord>();
        List<RedisNode> currentNodes = cluster.getAllAliveNodes();
        int totalNodes = currentNodes.size();
        int avgSlots = Slots.TOTAL_SLOTS / totalNodes;
        
        cluster.loadClusterInfo();
        
        
        int need=0;
        for(int i = 0; i < addedNodes.size(); i++){
        	RedisNode currNode = addedNodes.get(i);
        	
			for (RedisNode clusterNode : cluster.getNodes()) {
				if (clusterNode.getMaster().equals(currNode.getMaster())) {
					currNode = clusterNode;
				}
			}
        	
        	need = avgSlots;
        	for(RedisNode redisNode : currentNodes){
        		if(!currNode.equals(redisNode) && redisNode.getMaster().getSlotList().size() > avgSlots){
        			ReshardRecord rr = new ReshardRecord();
                	rr.setDestNode(currNode);
                	rr.setSourceNode(redisNode);
                	List<Integer> slots = new ArrayList<Integer>();
                	rr.setSlot(slots);
                	
                	List<Integer> sourceList = rr.getSourceNode().getMaster().getSlotList();
                	int sourSlotsNum = sourceList.size();
                	
                	while(sourSlotsNum >= avgSlots  && need > 0 ){
                		slots.add(sourceList.get(sourceList.size() - sourSlotsNum));
                		sourSlotsNum--;
                		need--;
                	}
                	reshardRecord.add(rr);
        		}
        	}
        	
        	
        }
        return reshardRecord;
    }

	@Override
	public void removeNode(RedisNode node) throws ScaleException {
		// TODO Auto-generated method stub
		
	}

	public List<RedisNode> getDeledNodes() {
		return deledNodes;
	}

	public void setDeledNodes(List<RedisNode> deledNodes) {
		this.deledNodes = deledNodes;
	}
    
}
