package com.dianping.cache.scale.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisClusterException;

import com.dianping.cache.autoscale.AppId;
import com.dianping.cache.autoscale.AutoScale;
import com.dianping.cache.autoscale.Instance;
import com.dianping.cache.autoscale.Result;
import com.dianping.cache.autoscale.dockerscale.DockerScale;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.Server;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.ServerClusterService;
import com.dianping.cache.service.ServerService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.ParseServersUtil;

public class RedisUtil {

    public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
    public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
    public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
    public static final long CLUSTER_SLEEP_INTERVAL = 50;
    public static final int CLUSTER_DEFAULT_TIMEOUT = 300;
    public static final int CLUSTER_DEFAULT_DB = 0;
    public static final String UNIX_LINE_SEPARATOR = "\n";
    public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
    public static final String COLON_SEPARATOR = ":";

    private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private static AutoScale autoScale = new DockerScale();

    private static volatile int operateId = 0;

    private static Map<Integer, Integer> scaleStatus = new HashMap<Integer, Integer>();

    public static int applyNodes(String appId, int number) {
        return autoScale.scaleUp(AppId.valueOf(appId), number);
    }

    public static Result getResult(int operateId) {
        return autoScale.getValue(operateId);
    }

    public static int scaleNode(String cluster, int nodeNum, String appId) {
        int totalNum = nodeNum * 2; // totalNum = master + slave
        int scaleOperationId = operateId++;
        scaleStatus.put(scaleOperationId, 100);
        autoScaleNode(scaleOperationId, appId, cluster, totalNum);
        return scaleOperationId;
    }

    public static int scaleSlave(String cluster,String masterAddress) {
        int scaleOperationId = operateId++;
        scaleStatus.put(scaleOperationId, 100);
        autoScaleSlave(scaleOperationId, cluster,masterAddress);
        return scaleOperationId;
    }

    public static int deleteMaster(String cluster, String address) {
        int scaleOperationId = operateId++;
        scaleStatus.put(scaleOperationId, 100);
        //TODO
        return scaleOperationId;
    }

    public static boolean deleteSlave(String cluster, String address) {
        return removeNode(new RedisServer(address));
    }

    public static int getOperateStatus(int scaleOperationId) {
        return scaleStatus.get(scaleOperationId);
    }


    private static void autoScaleNode(final int scaleOperationId, final String appId, final String cluster, final int totalNum) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                int operateid = autoScale.scaleUp(AppId.valueOf(appId), totalNum);
                Result result = autoScale.getValue(operateid);
                while (result.getStatus() == 100) {
                    try {
                        Thread.sleep(100);
                        result = autoScale.getValue(operateid);
                    } catch (InterruptedException e) {
                        throw new ScaleException(e.toString());
                    }
                }
                scaleStatus.put(scaleOperationId, result.getStatus());
                if (result.getStatus() == 200) {
                    joinCluster(scaleOperationId, cluster, result);
                }
            }
        };
        Thread t = new Thread(run, "ScaleNode");
        t.start();
    }

    private static void autoScaleSlave(final int scaleOperationId,final String cluster, final String masterAddress) {
        // TODO Auto-generated method stub
        ServerService serverService = SpringLocator.getBean("serverService");
        Server server = serverService.findByAddress(masterAddress);
        final AppId appId = AppId.valueOf(server.getAppId());

        Runnable run = new Runnable() {
            @Override
            public void run() {
                int operateid = autoScale.scaleUp(appId, 1);
                Result result = autoScale.getValue(operateid);
                while (result.getStatus() == 100) {
                    try {
                        Thread.sleep(100);
                        result = autoScale.getValue(operateid);
                    } catch (InterruptedException e) {
                        //if node is already scaled , it has type -2 , manaul detroy it
                        throw new ScaleException(e.toString());
                    }
                }
                scaleStatus.put(scaleOperationId, result.getStatus());
                if (result.getStatus() == 200 && result.getInstances().size() == 1) {
                    beSlaveOfMaster(scaleOperationId, masterAddress, result);
                    postJoinCLuster(cluster,result.getInstances().get(0).getIp()+":"+result.getAppId().getPort());
                }
            }
        };
        Thread t = new Thread(run, "ScaleSlave");
        t.start();
    }

    private static void beSlaveOfMaster(int scaleOperationId, String masterAddress, Result result) {
        // just one slave to join master node
        try {
            replicas(masterAddress, result.getInstances().get(0).getIp() + ":" + result.getAppId().getPort());
        } catch (Exception e) {
            logger.error("Add slave to " + masterAddress + " error ! Please check : " + result.getInstances().get(0).getIp() + "\n" + e);
        } finally {
        }

    }

    private static void joinCluster(final int scaleOperationId, final String cluster, final Result result) {

        String randomAddress = getRandomAddress(cluster);
        for (int i = 0; i < result.getInstances().size(); i += 2) {
            String master = result.getInstances().get(i).getIp() + ":" + result.getAppId().getPort();
            String slave = result.getInstances().get(i + 1).getIp() + ":" + result.getAppId().getPort();

            try {
                if (joinCluster(scaleOperationId, randomAddress, master)) {
                    postJoinCLuster(cluster,master);
                    replicas(master, slave);
                    postJoinCLuster(cluster,slave);

                } else {
                    //the master join cluster maybe failed ， destroy slave or not ~

                }
            } catch (Exception e) {
                logger.error("Join cluster error ! Please check : \n" + e);
            }
        }
    }

    private static void replicas(String master, String slave) {
        RedisServer m = new RedisServer(master);
        RedisServer s = new RedisServer(slave);
        String masterNodeId = getNodeId(m);
        Jedis mJedis = new Jedis(m.getIp(), m.getPort());
        mJedis.clusterMeet(s.getIp(), s.getPort());
        waitForClusterReady(master);
        Jedis sJedis = new Jedis(s.getIp(), s.getPort());
        sJedis.clusterReplicate(masterNodeId);
        sJedis.close();
    }

    private static boolean joinCluster(final int scaleOperationId, final String clusterNodeAddress, final String nodeToJoin) {

        RedisServer clusterNode = new RedisServer(clusterNodeAddress);
        RedisServer joinNode = new RedisServer(nodeToJoin);

        Jedis clusterJedis = new Jedis(clusterNode.getIp(), clusterNode.getPort());
        clusterJedis.clusterMeet(joinNode.getIp(), joinNode.getPort());

        List<RedisServer> clusterNodes = getAllNodesOfCluster(clusterNode);

        boolean joinOk = false;
        long sleepTime = 0;
        while (!joinOk && sleepTime < CLUSTER_DEFAULT_TIMEOUT) {
            joinOk = true;
            for (RedisServer rs : clusterNodes) {
                if (!isNodeKnown(rs, joinNode)) {
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
        clusterJedis.close();
        return joinOk;
    }

    private static boolean isNodeKnown(RedisServer srcNodeInfo, RedisServer targetNodeInfo) {
        Jedis srcNode = new Jedis(srcNodeInfo.getIp(), srcNodeInfo.getPort());
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


    private static List<String> loadNode(Result result) {
        // TODO 分配主从节点 ：  分配节点应遵循 所有的主节点分散在不同的物理机上， 主从节点不能在同一台物理机上
        List<String> join = new ArrayList<String>();
        for (Instance ins : result.getInstances()) {
            join.add(ins.getIp() + ":" + result.getAppId().getPort());
        }
        /**如果两个主从节点在同一台物理机上   则 throws   DuplicateHostInNodeException 
         **/
        /**分配算法   利用一个桶  TreeMap 将宿主机ip相同的都放到同一个桶中，第一次选取master  依次将桶中的ip 放入返回结果中
         * 第二次  从下标为 1 的map中依次选取 slave
         * 分配完成后检测有没有 主从在同一物理机的   如果有  index＋1 重复第二步  直到index == TreeMap size
         **/


        return join;
    }


    public static String getNodeId(final RedisServer node) {
        Jedis jedis = new Jedis(node.getIp(), node.getPort());
        String[] clusterInfo = splitClusterInfo(jedis.clusterNodes());
        for (String lineInfo : clusterInfo) {
            if (lineInfo.contains("myself")) {
                jedis.close();
                return lineInfo.split(" ")[0];
            }
        }
        jedis.close();
        return null;
    }


    public static boolean removeNode(final RedisServer nodeToDelete) {
        Jedis deleteNode = new Jedis(nodeToDelete.getIp(), nodeToDelete.getPort());
        String deleteNodeId = getNodeId(nodeToDelete);
        List<RedisServer> allNodesOfCluster = getAllNodesOfCluster(nodeToDelete);

        // check if the node to delete is a master
        boolean isMaster = false;
        if (nodeToDelete.isMaster()) {
            isMaster = true;
        }

        // the node to delete is slave
        if (!isMaster) {
            for (RedisServer node : allNodesOfCluster) {
                // a node cannot `forget` itself
                if (!node.equals(nodeToDelete)) {
                    Jedis conn = new Jedis(node.getIp(), node.getPort());
                    conn.clusterForget(deleteNodeId);
                    conn.close();
                }
            }
            return true;
        }

//		// the node to delete is master
//		List<Integer> availableSlots = nodeToDelete.getSlotList();
//		// no slots on the master
//		if (!availableSlots.isEmpty()) {
//			int slotsToEachMaster = availableSlots.size() / (allNodesOfCluster.size()-1);
//			int remainSlots = availableSlots.size() - slotsToEachMaster * (allNodesOfCluster.size()-1);
//
//			for (int i = 0; i < allNodesOfCluster.size(); i++) {
//				if( !nodeToDelete.equals(allNodesOfCluster.get(i).getMaster())){
//					migrate(nodeToDelete, allNodesOfCluster.get(i).getMaster(), slotsToEachMaster);
//					if (i == allNodesOfCluster.size() - 1 && remainSlots > 0) {
//						migrate(nodeToDelete, allNodesOfCluster.get(i).getMaster(), remainSlots);
//					}
//				}
//			}
//		}
//
//		List<String> slavesOfMaster = deleteNode.clusterSlaves(deleteNodeId);
//		//forget nodes
//		for (RedisNode nodeInfo: allNodesOfCluster) {
//			RedisServer master = nodeInfo.getMaster();
//			RedisServer slave = nodeInfo.getSlave();
//			if (!master.equals(nodeToDelete)) {
//				Jedis node = RedisConnectionFactory.getConnection(master.getAddress());
//				node.clusterForget(deleteNodeId);
//				for (String slaveStr: slavesOfMaster) {
//					node.clusterForget(slaveStr.split(" ")[0]);
//				}
//			}
//			if (slave != null && !slave.equals(nodeToDelete)) {
//				Jedis node = RedisConnectionFactory.getConnection(slave.getAddress());
//				node.clusterForget(deleteNodeId);
//				for (String slaveStr: slavesOfMaster) {
//					node.clusterForget(slaveStr.split(" ")[0]);
//				}
//			}
//		}
//		cluster.loadClusterInfo();
        deleteNode.close();
        return true;
    }

    public static List<RedisServer> getAllNodesOfCluster(final RedisServer nodeInfo) {
        Jedis node = new Jedis(nodeInfo.getIp(), nodeInfo.getPort());
        List<RedisServer> clusterNodeList = new ArrayList<RedisServer>();
        String[] clusterNodesOutput = node.clusterNodes().split(UNIX_LINE_SEPARATOR);
        for (String infoLine : clusterNodesOutput) {
            String[] hostAndPort = infoLine.split(" ")[1].split(":");
            RedisServer rs = new RedisServer(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
            clusterNodeList.add(rs);
        }
        return clusterNodeList;
    }

    public static void waitForClusterReady(final List<RedisNode> clusterNodes) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            for (RedisNode rNode : clusterNodes) {
                if (rNode.getMaster() != null) {
                    Jedis master = RedisConnectionFactory.getConnection(rNode.getMaster().getAddress());
                    String clusterInfo = master.clusterInfo();
                    String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                    String[] firstLineArr = firstLine.trim().split(COLON_SEPARATOR);
                    if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                            firstLineArr[1].equalsIgnoreCase("ok")) {
                        if (rNode.getSlave() != null) {
                            Jedis slave = RedisConnectionFactory.getConnection(rNode.getSlave().getAddress());
                            clusterInfo = slave.clusterInfo();
                            firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                            firstLineArr = firstLine.split(COLON_SEPARATOR);
                            if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                                    firstLineArr[1].equalsIgnoreCase("ok")) {
                                continue;
                            }
                            clusterOk = false;
                            break;
                        }
                        continue;
                    }
                    clusterOk = false;
                    break;
                }
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

    public static void waitForClusterReady(String clusterAddress) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            List<RedisServer> serverList = getAllNodesOfCluster(new RedisServer(clusterAddress));
            for (RedisServer rs : serverList) {
                Jedis jedis = new Jedis(rs.getIp(), rs.getPort());
                String clusterInfo = jedis.clusterInfo();
                String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                String[] firstLineArr = firstLine.trim().split(COLON_SEPARATOR);
                if (firstLineArr[0].equalsIgnoreCase("cluster_state") &&
                        firstLineArr[1].equalsIgnoreCase("ok")) {
                    jedis.close();
                    continue;
                }
                jedis.close();
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

    public static void destroy(String appId, String address) {
        autoScale.scaleDown(address);
    }

    public static void des(String instanceId) {
        //autoScale.destroyByInstanceId(instanceId);
    }

    private static String[] splitClusterInfo(String str) {
        if (str != null) {
            str = str.replaceAll("\r\n", "#");
            str = str.replaceAll("\n", "#");
            return str.split("#");
        }
        return null;
    }

    private static void postJoinCLuster(String cluster,String address) {
        ServerService serverService = SpringLocator.getBean("serverService");
        Server server = serverService.findByAddress(address);
        server.setType(1);
        serverService.update(server);

        ServerClusterService serverClusterService = SpringLocator.getBean("serverClusterService");
        serverClusterService.insert(address,cluster);
    }

    private static String getRandomAddress(String cluster){
        CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
        CacheConfiguration cacheConfiguration = cacheConfigurationService.find(cluster);
        if(cacheConfiguration != null){
            List<String> servers = ParseServersUtil.parseRedisServers(cacheConfiguration.getServers());
            return servers.get(0);
        }
        return null;
    }
}
