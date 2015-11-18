package com.dianping.cache.scale.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisClusterException;
import redis.clients.util.ClusterNodeInformation;
import redis.clients.util.ClusterNodeInformationParser;

public class ClusterUtil {
    public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
    public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
    public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
    public static final long CLUSTER_SLEEP_INTERVAL = 100;
    public static final int CLUSTER_DEFAULT_TIMEOUT = 300;
    public static final int CLUSTER_MIGRATE_NUM = 100;
    public static final int CLUSTER_DEFAULT_DB = 0;
    public static final String UNIX_LINE_SEPARATOR = "\n";
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String COLON_SEPARATOR = ":";

    /**
     * wait for the cluster to be ready;
     * the cluster is ready when `cluster info` of all nodes {@code clusterNodes} are ok.
     *
     * @param clusterNodes   master nodes
     */
    public static void waitForClusterReady(final Set<HostAndPort> clusterNodes) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            for (HostAndPort hnp: clusterNodes) {
                Jedis node = new Jedis(hnp.getHost(), hnp.getPort());
                String clusterInfo = node.clusterInfo();
                String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                node.close();
                String[] firstLineArr = firstLine.split(COLON_SEPARATOR);
                if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                        firstLineArr[1].equalsIgnoreCase("ok")) {
                    continue;
                }
                clusterOk = false;
                break;
            }

            if (clusterOk) {
                break;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new JedisClusterException("waitForClusterReady", e);
            }
        }
    }

    /**
     * check if the node {@code srcNodeInfo} already 'knows' the target node {@code targetNodeInfo},
     * which means whether they are in the same cluster;
     *
     * @param srcNodeInfo  the src node
     * @param targetNodeInfo  the dest node
     * @return  if 'known' return true, else return false
     */
    public static boolean isNodeKnown(final HostAndPort srcNodeInfo, final HostAndPort targetNodeInfo) {
        Jedis srcNode = new Jedis(srcNodeInfo.getHost(), srcNodeInfo.getPort());
        String targetNodeId = getNodeId(targetNodeInfo);
        String[] clusterInfo = srcNode.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine : clusterInfo) {
            if (infoLine.contains(targetNodeId)) {
                srcNode.close();
                return true;
            }
        }
        srcNode.close();
        return false;
    }

    /**
     * put node {@code nodeToJoin} in the cluster represented by node {@code clusterNodeInfo};
     *
     * @param clusterNodeInfo       one node in the cluster
     * @param nodeToJoin            the node to add
     * @param timeoutMs             timeout in ms
     */
    public static boolean joinCluster(final HostAndPort clusterNodeInfo, final HostAndPort nodeToJoin, final long timeoutMs) {
        Jedis clusterNode = new Jedis(clusterNodeInfo.getHost(), clusterNodeInfo.getPort());
        clusterNode.clusterMeet(nodeToJoin.getHost(), nodeToJoin.getPort());
        List<HostAndPort> clusterNodes = getAllNodesOfCluster(clusterNodeInfo);

        // wait for join ok
        boolean joinOk = false;
        long sleepTime = 0;
        while (!joinOk && sleepTime < timeoutMs) {
            joinOk = true;
            for (HostAndPort hnp: clusterNodes) {
                if (!isNodeKnown(hnp, nodeToJoin)) {
                    joinOk = false;
                    break;
                }
            }
            if (joinOk) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(CLUSTER_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new JedisClusterException("joinCluster timeout.", e);
            }
            sleepTime += CLUSTER_SLEEP_INTERVAL;
        }
        clusterNode.close();
        return joinOk;
    }

    /**
     * put the node {@code nodeToJoin} to the cluster with default timeout
     *
     * @param clusterNodeInfo   one node in the cluster
     * @param nodeToJoin        the node to join
     * @return
     */
    public static boolean joinCluster(final HostAndPort clusterNodeInfo, final HostAndPort nodeToJoin) {
        return joinCluster(clusterNodeInfo, nodeToJoin, CLUSTER_DEFAULT_TIMEOUT);
    }

    /**
     * make nodes {@code slaves} be the slave of the {@code master} node
     *
     * @param master    the master node
     * @param slaves    slave node list
     */
    public static void beSlaveOfMaster(final HostAndPort master, final List<HostAndPort> slaves) {
        String masterNodeId = getNodeId(master);
        for (HostAndPort slave: slaves) {
            Jedis slaveNode = new Jedis(slave.getHost(), slave.getPort());
            slaveNode.clusterReplicate(masterNodeId);
            slaveNode.close();
        }
    }

    /**
     * get node info of the current node (myself) from `cluster nodes` output info
     *
     * @param nodeInfo  the node
     */
    public static String getNodeInfo(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        String[] clusterInfo = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String lineInfo: clusterInfo) {
            if (lineInfo.contains("myself")) {
                return lineInfo;
            }
        }
        node.close();
        return "";
    }

    /**
     * get the node id of the current node {@code nodeInfo}
     *
     * @param nodeInfo  the node
     * @return  node id
     */
    public static String getNodeId(final HostAndPort nodeInfo) {
        Jedis jedis = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        String[] clusterInfo = jedis.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String lineInfo: clusterInfo) {
            if (lineInfo.contains("myself")) {
                return lineInfo.split(COLON_SEPARATOR)[0];
            }
        }
        return "";
    }

    /**
     * get all nodes from the cluster
     *
     * be care, sometimes the output likes this:
     *
     * fd80d1696a8af7c6148db3a824dadbb09622227a :8000 myself,master - 0 0 0 connected 0-16300
     * 0ef0b665a18723b6384d93dbc886b97e90c100db 10.7.40.49:8002 master - 0 1414050055100 2 connected 16301-16383
     * a31f4967b88f2af6a4d6637fe420c76ee9a91b83 10.7.40.49:8003 slave 0ef0b665a18723b6384d93dbc886b97e90c100db 0 1414050056101 2 connected
     * d08e6b9f7f32dcc5556b5395227e0afeadc0c836 10.7.40.49:8001 slave fd80d1696a8af7c6148db3a824dadbb09622227a 0 1414050054098 1 connected
     *
     * @param nodeInfo   one node of the cluster
     * @return
     */
    public static List<HostAndPort> getAllNodesOfCluster(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        List<HostAndPort> clusterNodeList = new ArrayList<HostAndPort>();
        clusterNodeList.add(nodeInfo);
        String[] clusterNodesOutput = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine: clusterNodesOutput) {
            if (infoLine.contains("myself")) {
                continue;
            }
            String[] hostAndPort = infoLine.split(" ")[1].split(":");
            HostAndPort hnp = new HostAndPort(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
            clusterNodeList.add(hnp);
        }
        return clusterNodeList;
    }

    /**
     * get all master nodes of the cluster;
     *
     * @param nodeInfo   one node of the cluster
     * @return
     */
    public static List<HostAndPort> getMasterNodesOfCluster(final HostAndPort nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getHost(), nodeInfo.getPort());
        List<HostAndPort> masterNodeList = new ArrayList<HostAndPort>();

        String[] clusterNodesOutput = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine: clusterNodesOutput) {
            if (infoLine.contains("master")) {
                if (infoLine.contains("myself")) {
                    masterNodeList.add(nodeInfo);
                } else {
                    String[] hostAndPort = infoLine.split(" ")[1].split(":");
                    masterNodeList.add(new HostAndPort(hostAndPort[0], Integer.valueOf(hostAndPort[1])));
                }
            }
        }
        node.close();
        return masterNodeList;
    }


    /**
     * allocate empty slots {@code slots} to the master nodes {@code masterNodes},
     * evenly to the best.
     *
     * @param slots         the slots to allocate
     * @param masterNodes   the master nodes
     */
    public static void allocateSlotsToNodes(final List<Integer> slots, final List<HostAndPort> masterNodes) {
        int numOfMaster = masterNodes.size();
        int slotsPerNode = slots.size() / numOfMaster;
        int lastSlot = 0;
        for (int i = 0; i < numOfMaster; i++) {
            HostAndPort masterNodeInfo = masterNodes.get(i);
            Jedis node = new Jedis(masterNodeInfo.getHost(), masterNodeInfo.getPort());
            /** the last node */
            if (i == numOfMaster - 1) {
                slotsPerNode = slots.size() - slotsPerNode * i;
            }
            int[] slotArray = new int[slotsPerNode];
            for (int k = lastSlot, j = 0; k < (i + 1) * slotsPerNode && j < slotsPerNode; k++, j++) {
                slotArray[j] = slots.get(k);
            }
            lastSlot = (i + 1) * slotsPerNode;
            node.clusterAddSlots(slotArray);
            node.close();
        }
    }

    /**
     * wait for the migration process done: check the output of `cluster nodes` make sure
     *  that migration is done.
     *
     * migration-in-transition:
     *  38807bd0262d99f205ebd0eb3e483cc09e927731 :7002 myself,master - 0 0 1 connected 0-5459 [5460->-38807bd0262d99f205ebd0eb3e483cc09e927731] [5461-<-e85a79cfee516d9eb1339e8f0107466307b4a50c]
     *
     * @param nodesInfo     the nodes to check
     */
    public static void waitForMigrationDone(final HostAndPort nodesInfo) {
        checkNotNull(nodesInfo, "nodesInfo is null.");

        Jedis node = new Jedis(nodesInfo.getHost(), nodesInfo.getPort());
        String[] clusterNodesInfo = node.clusterNodes().split(UNIX_LINE_SEPARATOR);

        boolean isOk = false;
        while (!isOk) {
            isOk = true;
            for (String infoLine: clusterNodesInfo) {
                if (infoLine.startsWith(SLOT_IN_TRANSITION_IDENTIFIER)) {
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

    /**
     * get slots information of the node, especially the serving slots.
     *
     * @param nodeInfo   the node info
     * @return           the slots info of the node
     */
    public static ClusterNodeInformation getNodeSlotsInfo(final HostAndPort nodeInfo) {
        checkNotNull(nodeInfo, "nodeInfo is null.");

        ClusterNodeInformationParser parser = new ClusterNodeInformationParser();
        String nodeInfoLine = getNodeInfo(nodeInfo);
        ClusterNodeInformation nodeInformation = parser.parse(nodeInfoLine,
                new redis.clients.jedis.HostAndPort(nodeInfo.getHost(), nodeInfo.getPort()));
        return nodeInformation;
    }
    
	
	public static void main(String[] ad){
		Jedis node = RedisConnectionFactory.getConnection("192.168.224.71:7000");
		List<String> config = node.configGet("maxmemory");
		String config2 = node.clusterSaveConfig();
		return;
	}
}
