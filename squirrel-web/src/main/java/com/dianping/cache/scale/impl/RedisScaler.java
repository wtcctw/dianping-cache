package com.dianping.cache.scale.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisClusterException;

import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.ScalePlan;
import com.dianping.cache.scale.Scaler;

public class RedisScaler implements Scaler<RedisScalePlan>{
   
    public static final int CLUSTER_DEFAULT_TIMEOUT = 10000;
    public static final int CLUSTER_MIGRATE_NUM = 100;
    public static final long CLUSTER_SLEEP_INTERVAL = 10;
    public static final int CLUSTER_DEFAULT_DB = 0;
    public static final String UNIX_LINE_SEPARATOR = "\n";
    public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";

    private List<String> serverList;
    private List<String> addServerList;
    private RedisCluster cluster;
    
    public RedisScaler(List<String> serverList, List<String> addServerList) {
        this.serverList = serverList;
        this.addServerList = addServerList;
    }

    @Override
    public void scaleUp() throws ScaleException {
        cluster = new RedisCluster(serverList);
        cluster.loadClusterInfo();
        RedisScalePlan scalePlan = new RedisScalePlan(cluster);
        List<RedisNode> addNodes = getAddNodes(addServerList);
        scalePlan.addNodes(addNodes);
        execute(scalePlan);
    }
    
	@Override
	public void scaleDown() throws ScaleException {
		cluster = new RedisCluster(serverList);
        cluster.loadClusterInfo();
        RedisScalePlan scalePlan = new RedisScalePlan(cluster);
        List<RedisNode> delNodes = getAddNodes(addServerList);
        scalePlan.addNodes(delNodes);
        execute0(scalePlan);
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
        if(addServerList.size()==1){
        	RedisServer master = new RedisServer(addServerList.get(0));
        	RedisNode node = new RedisNode(master, null);
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
            RedisUtil.waitForClusterReady(cluster.getNodes());
            cluster.loadClusterInfo();
        }
        RedisUtil.waitForClusterReady(cluster.getNodes());
        cluster.loadClusterInfo();
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
        Jedis jedis = RedisConnectionFactory.getConnection(node.getMaster().getAddress());
        String status = jedis.clusterMeet(server.getIp(), server.getPort());
        if(!"OK".equals(status)) {//这个 ok   貌似只管 meet 发送成功
            throw new ScaleException(server + " failed to join cluster: " + status);
        }
    }

    boolean replicate(RedisNode node) {
        RedisServer slave = node.getSlave();
        Jedis jedis = RedisConnectionFactory.getConnection(slave.getAddress());
        String result = jedis.clusterReplicate(RedisUtil.getNodeId(node.getMaster()));
        return "OK".equals(result);
    }
    
    void reshard(ReshardRecord rr) throws ScaleException {
        RedisNode from = rr.getSourceNode();
        RedisNode to = rr.getDestNode();
        List<Integer> slots = rr.getSlots();
        for(int slot : slots) {
            migrate(from, to, slot);
        }
    }

	void migrate(RedisNode from, RedisNode to, int slot) throws ScaleException {
		migrate(from.getMaster(),to.getMaster(),slot);
	}
	
	void migrate(RedisServer from,RedisServer to,int slot) throws ScaleException{

		Jedis srcNode = RedisConnectionFactory.getConnection(from.getAddress());
		String srcNodeId = from.getId();
		Pipeline pipeline = srcNode.pipelined();
		Jedis destNode = RedisConnectionFactory.getConnection(to.getAddress());
		String destNodeId = to.getId();

		/** migrate every slot from src node to dest node */
		destNode.clusterSetSlotImporting(slot, srcNodeId);
		srcNode.clusterSetSlotMigrating(slot, destNodeId);

		while (true) {
			List<String> keysInSlot = srcNode.clusterGetKeysInSlot(slot,CLUSTER_MIGRATE_NUM);
			if (keysInSlot.isEmpty()) {
				break;
			}
			for (String key : keysInSlot) {
				pipeline.migrate(to.getIp(),to.getPort(), key,
						CLUSTER_DEFAULT_DB,CLUSTER_DEFAULT_TIMEOUT);
			}
			pipeline.sync();
		}
		
		notifyAllNode(slot, destNodeId);
		/** wait for slots migration done */
		waitForMigrationDone(from);
		waitForMigrationDone(to);
	}
	
	private void notifyAllNode(int slot,String destNodeId) throws ScaleException{
		cluster.loadClusterInfo();
		List<RedisNode> nodes = cluster.getAllAliveNodes();
		for(RedisNode node : nodes){
			if(node.getMaster() != null){
				Jedis jnode = RedisConnectionFactory.getConnection(node.getMaster().getAddress());	
				jnode.clusterSetSlotNode(slot, destNodeId);
			}
			if(node.getSlave() != null){
				Jedis jnode = RedisConnectionFactory.getConnection(node.getSlave().getAddress());	
				jnode.clusterSetSlotNode(slot, destNodeId);
			}
		}
	}


    private void execute0(RedisScalePlan scalePlan) throws ScaleException {
     	RedisServer nodeToDelete = null;
     	removeNode(nodeToDelete);
 	}
     
    public  void removeNode(final RedisServer nodeToDelete) throws ScaleException {
         Jedis deleteNode = new Jedis(nodeToDelete.getIp(), nodeToDelete.getPort());
         String deleteNodeId = nodeToDelete.getId();
         List<RedisNode> allNodesOfCluster = cluster.getAllAliveNodes();
         
         // check if the node to delete is a master
         boolean isMaster = false;
         if (nodeToDelete.isMaster()) {
             isMaster = true;
         }

         // the node to delete is slave
         if (!isMaster) {
             for (RedisNode nodeInfo: allNodesOfCluster) {
                 // a node cannot `forget` itself
             	RedisServer master = nodeInfo.getMaster();
             	RedisServer slave = nodeInfo.getSlave();
                 if (!master.equals(nodeToDelete)) {
                     Jedis node = RedisConnectionFactory.getConnection(master.getAddress());
                     node.clusterForget(deleteNodeId);
                 }
                 if (slave != null && !slave.equals(nodeToDelete)) {
                     Jedis node = RedisConnectionFactory.getConnection(slave.getAddress());
                     node.clusterForget(deleteNodeId);
                 }
             }
             cluster.loadClusterInfo();
             return;
         }

         // the node to delete is master
         List<Integer> availableSlots = nodeToDelete.getSlotList();
         // no slots on the master
         if (!availableSlots.isEmpty()) {
             int slotsToEachMaster = availableSlots.size() / (allNodesOfCluster.size()-1);
             int remainSlots = availableSlots.size() - slotsToEachMaster * (allNodesOfCluster.size()-1);

             for (int i = 0; i < allNodesOfCluster.size(); i++) {
            	 if( !nodeToDelete.equals(allNodesOfCluster.get(i).getMaster())){
            		 migrate(nodeToDelete, allNodesOfCluster.get(i).getMaster(), slotsToEachMaster);
            		 if (i == allNodesOfCluster.size() - 1 && remainSlots > 0) {
            			 migrate(nodeToDelete, allNodesOfCluster.get(i).getMaster(), remainSlots);
            		 }
            	 }
             }
         }

         List<String> slavesOfMaster = deleteNode.clusterSlaves(deleteNodeId);
         //forget nodes
         for (RedisNode nodeInfo: allNodesOfCluster) {
         	RedisServer master = nodeInfo.getMaster();
         	RedisServer slave = nodeInfo.getSlave();
             if (!master.equals(nodeToDelete)) {
                 Jedis node = RedisConnectionFactory.getConnection(master.getAddress());
                 node.clusterForget(deleteNodeId);
                 for (String slaveStr: slavesOfMaster) {
                     node.clusterForget(slaveStr.split(" ")[0]);
                 }
             }
             if (slave != null && !slave.equals(nodeToDelete)) {
                 Jedis node = RedisConnectionFactory.getConnection(slave.getAddress());
                 node.clusterForget(deleteNodeId);
                 for (String slaveStr: slavesOfMaster) {
                     node.clusterForget(slaveStr.split(" ")[0]);
                 }
             }
         }
         cluster.loadClusterInfo();
         deleteNode.close();
     }
     
     public void waitForMigrationDone(final RedisServer nodesInfo) {
         checkNotNull(nodesInfo, "nodesInfo is null.");

         Jedis node = RedisConnectionFactory.getConnection(nodesInfo.getAddress());
         String[] clusterNodesInfo = node.clusterNodes().split(UNIX_LINE_SEPARATOR);

         boolean isOk = false;
         while (!isOk) {
         	clusterNodesInfo = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
             isOk = true;
             for (String infoLine: clusterNodesInfo) {
                 if (infoLine.contains(SLOT_IN_TRANSITION_IDENTIFIER)) {
                     isOk = false;
                     break;
                 }
             }
             if (isOk) {
                 break;
             }
             try {
                 TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
             } catch (InterruptedException e) {
                 throw new JedisClusterException("waitForMigrationDone", e);
             }
         }
     }
}
