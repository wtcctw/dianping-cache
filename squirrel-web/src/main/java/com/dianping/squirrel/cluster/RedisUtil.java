package com.dianping.squirrel.cluster;

import com.dianping.cache.scale.ScaleException;
import com.dianping.squirrel.cluster.redis.Slot;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisClusterException;
import redis.clients.util.ClusterNodeInformation;
import redis.clients.util.ClusterNodeInformationParser;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/5/4.
 */
public class RedisUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);


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


    public static Cluster createCluster(List<RawDataNode> rawDataNodes,int masterCount, int slaveCount){
        if(rawDataNodes == null || rawDataNodes.size() != (slaveCount + 1) * masterCount){
            logger.error("create cluster failed, wrong number of redis nodes .",rawDataNodes);
            return null;
        }

        for(RawDataNode rawDataNode : rawDataNodes){
            if(!isRedisAvailable(rawDataNode.getIp(),rawDataNode.getPort(),10000)){
                logger.error("create cluster failed, timeout for waiting redis node available",rawDataNode);
                return null;
            }
        }


        Map<String,ArrayList<RawDataNode>> hostNodesMap = new HashMap<String, ArrayList<RawDataNode>>();
        for(RawDataNode rawDataNode : rawDataNodes){
            String hostIp = rawDataNode.getHostIp();
            ArrayList<RawDataNode> rawDataNodeArrayList = hostNodesMap.get(hostIp);
            if(rawDataNodeArrayList == null){
                rawDataNodeArrayList = new ArrayList<RawDataNode>();
                hostNodesMap.put(hostIp,rawDataNodeArrayList);
            }
            rawDataNodeArrayList.add(rawDataNode);
        }

        ValueComparator valueComparator = new ValueComparator(hostNodesMap);
        Map<String,ArrayList<RawDataNode>> sorted = new TreeMap<String, ArrayList<RawDataNode>>(valueComparator);
        sorted.putAll(hostNodesMap);

        int total = rawDataNodes.size();
        int clusterNodeCount = total / (slaveCount + 1);


        if(sorted.size() < clusterNodeCount){
            logger.error("create cluster failed, multi master will meeting in on host!");
            return null;
        }

        int index = 0;

        Map<String,Integer> mapListIndex = new HashMap<String, Integer>();
        for (Map.Entry<String,ArrayList<RawDataNode>> entry : sorted.entrySet()){
            mapListIndex.put(entry.getKey(),0);
        }
        Map<HostAndPort,ArrayList<HostAndPort>> clusterNodes = new LinkedHashMap<HostAndPort, ArrayList<HostAndPort>>();
        for(Map.Entry<String,ArrayList<RawDataNode>> entry : sorted.entrySet()){
            if(index == clusterNodeCount){
                break;
            }
            index++;
            clusterNodes.put(entry.getValue().get(0).getHostAndPort(),new ArrayList<HostAndPort>());
            mapListIndex.put(entry.getKey(),mapListIndex.get(entry.getKey())+1);
        }
        //add slave,  保证主从节点不在一个物理机上  保证同一个master的slave 尽量分散
        if(total != clusterNodeCount){
            for(Map.Entry<String,ArrayList<RawDataNode>> entry : sorted.entrySet()){
                String host = entry.getKey();
                List<RawDataNode> valueList = entry.getValue();
                int remain = valueList.size() - mapListIndex.get(host);
                if(remain > 0){
                    List<RawDataNode> remainList = valueList.subList(mapListIndex.get(host),valueList.size());
                    mapListIndex.put(host,valueList.size());
                    boolean flag = false;
                    for(Map.Entry<String,ArrayList<RawDataNode>> entry2 : sorted.entrySet()){
                        if(entry2.getKey().equals(host)){
                            flag = true;
                            continue;
                        }
                        HostAndPort masterHP = entry2.getValue().get(0).getHostAndPort();
                        if(flag
                                && remain > 0
                                && clusterNodes.containsKey(masterHP)
                                && clusterNodes.get(masterHP).size() < slaveCount){
                            clusterNodes.get(masterHP).add(remainList.get(remain-1).getHostAndPort());
                            remain--;
                        }
                    }
                    while (remain > 0){
                        for(Map.Entry<String,ArrayList<RawDataNode>> entry2 : sorted.entrySet()){

                            HostAndPort masterHP = entry2.getValue().get(0).getHostAndPort();
                            if(remain > 0
                                    && !entry2.getKey().equals(host)
                                    && clusterNodes.containsKey(masterHP)
                                    && clusterNodes.get(masterHP).size() < slaveCount){
                                clusterNodes.get(masterHP).add(remainList.get(remain-1).getHostAndPort());
                                remain--;
                            }
                        }
                    }
                }
            }

        }

        create(clusterNodes);
        logger.info("create cluster success. ");
        return null;
    }


    public static Cluster parseCluster(String clusterName, List<HostAndPort> hostAndPorts, String password) {
        if (hostAndPorts == null || hostAndPorts.size() == 0) {
            throw new ScaleException("server list is empty");
        }
        int retry = 0;
        for (HostAndPort hostAndPort : hostAndPorts) {
            Jedis jedis = null;
            try {
                retry++;
                jedis = new Jedis(hostAndPort.getHost(),hostAndPort.getPort());
                if(StringUtils.isNotBlank(password)) {
                    jedis.auth(password);
                }
                String clusterInfo = jedis.clusterNodes();
                List<ClusterNode> nodes = parseClusterNodes(clusterInfo);
                Cluster cluster = new Cluster(clusterName,password,nodes,hostAndPorts);
                return cluster;
            } catch (Exception e) {
                if(retry >= 3){
                    logger.error("can't load cluster info,",e);
                    return null;
                }
                continue;
            } finally{
                if(jedis != null){
                    jedis.close();
                }
            }
        }
        return null;
    }

    public static List<ClusterNode> parseClusterNodes(String clusterNodesStr){
        List<DataNode> dataNodes = new ArrayList<DataNode>();
        for(String nodesInfo : clusterNodesStr.split("\n")) {
            DataNode dataNode = parseNodeInfo(nodesInfo);
            dataNodes.add(dataNode);
        }

        Map<String, ClusterNode> nodeMap = new HashMap<String, ClusterNode>();
        for(DataNode dataNode : dataNodes) {
            if(dataNode.isMaster()) {
                ClusterNode clusterNode = new ClusterNode();
                clusterNode.setMaster(dataNode);
                nodeMap.put(dataNode.getId(), clusterNode);
            }
        }
        for(DataNode dataNode : dataNodes) {
            if(dataNode.isSlave()  && !dataNode.isFail()) {
                ClusterNode clusterNode = nodeMap.get(dataNode.getMasterId());
                if(clusterNode != null) {
                    clusterNode.addSlave(dataNode);
                } else {
                    logger.error("failed to find master for " + dataNode);
                }
            }
        }
        return new ArrayList<ClusterNode>(nodeMap.values());
    }

    public static DataNode parseNodeInfo(String nodeInfo){
        String[] parts = nodeInfo.split(" ");
        DataNode dataNode = new DataNode(parts[1]);
        dataNode.setId(parts[0]);
        dataNode.setFlags(parts[2]);
        if(dataNode.isMaster()) {
            String[] slots = Arrays.copyOfRange(parts, 8, parts.length);
            dataNode.setSlot(stringToSlot(slots));
        }
        if(dataNode.isSlave()) {
            dataNode.setMasterId(parts[3]);
        }
        return dataNode;
    }


    public static Slot stringToSlot(String[] slots) {
        Slot slot =  new Slot();
        String slotString = null;
        if (slots != null) {
            for (String segment : slots) {
                if (slotString == null) {
                    slotString = segment;
                } else {
                    slotString += ("," + segment);
                }
            }
        }
        if(slotString == null){
            return null;
        }
        String[] segments = slotString.split(",");
        List<Integer> slotList = new ArrayList<Integer>();
        for (String segment : segments) {
            segment = segment.trim();
            if (StringUtils.isEmpty(segment))
                continue;
            int idx = segment.indexOf('-');
            if (idx == -1) {
                slotList.add(Integer.parseInt(segment));
            } else if (segment.startsWith("[")) { //TODO 正在传输
                slot.setMigating(true);
            } else {
                int end = Integer.parseInt(segment.substring(idx + 1).trim());
                int start = Integer.parseInt(segment.substring(0, idx).trim());
                if (end < start) {
                    start = start ^ end;
                    end = start ^ end;
                    start = start ^ end;
                }
                for (int i = start; i <= end; i++) {
                    slotList.add(i);
                }
            }
        }
        // deduplicate
        Collections.sort(slotList);
        List<Integer> newList = new ArrayList<Integer>();
        if (slotList.size() > 0) {
            newList.add(slotList.get(0));
        }
        for (int i = 1; i < slotList.size(); i++) {
            int n = slotList.get(i);
            if (n != slotList.get(i - 1)) {
                newList.add(n);
            }
        }

        slot.setSlots(newList);
        return slot;
    }


    public static boolean isRawRedisNode(String ip, int port){
        return false;
    }


    public static boolean isRedisAvailable(String ip, int port, int timeout){
        Jedis jedis = new Jedis(ip,port);
        Exception exception = null;
        int time = 0;
        while(time <= timeout){
            try {
                String pong = jedis.ping();
                if("PONG".equalsIgnoreCase(pong)){
                    return true;
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                exception = e;
                time += 500;
            }
        }
        logger.error("redis : "+ ip +":" +port+ " is unavailable.",exception);
        return false;
    }



    public static void create(final Map<HostAndPort,ArrayList<HostAndPort>> clusterNodes) {
        checkArgument(clusterNodes != null && clusterNodes.size() > 0, "invalid clusterNodes.");

        HostAndPort firstNode = null;
        for (Map.Entry<HostAndPort, ArrayList<HostAndPort>> pair: clusterNodes.entrySet()) {
            HostAndPort masterNodeInfo = pair.getKey();
            List<HostAndPort> slavesNodeInfo = pair.getValue();

            if (firstNode == null) {
                firstNode = masterNodeInfo;
                for(HostAndPort slave : slavesNodeInfo){
                    joinCluster(firstNode, slave);
                }
                continue;
            }
            joinCluster(firstNode, masterNodeInfo);
            for(HostAndPort slave : slavesNodeInfo){
                joinCluster(firstNode, slave);
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (HostAndPort master: clusterNodes.keySet()) {
            List<HostAndPort> slaveList = clusterNodes.get(master);
            beSlaveOfMaster(master, slaveList);
        }


        List<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < JedisCluster.HASHSLOTS; i++) {
            slots.add(i);
        }
        allocateSlotsToNodes(slots, Lists.newArrayList(clusterNodes.keySet()));

        waitForClusterReady(clusterNodes.keySet());
    }


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
                if (firstLineArr[0].contains("cluster_state") &&
                        firstLineArr[1].contains("ok")) {
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

    public static boolean joinCluster(final HostAndPort clusterNodeInfo, final HostAndPort nodeToJoin) {
        return joinCluster(clusterNodeInfo, nodeToJoin, CLUSTER_DEFAULT_TIMEOUT);
    }

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


    public static void beSlaveOfMaster(final HostAndPort master, final List<HostAndPort> slaves) {
        String masterNodeId = getNodeId(master);
        for (HostAndPort slave: slaves) {
            Jedis slaveNode = new Jedis(slave.getHost(), slave.getPort());
            slaveNode.clusterReplicate(masterNodeId);
            slaveNode.close();
        }
    }

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

    public static ClusterNodeInformation getNodeSlotsInfo(final HostAndPort nodeInfo) {
        checkNotNull(nodeInfo, "nodeInfo is null.");

        ClusterNodeInformationParser parser = new ClusterNodeInformationParser();
        String nodeInfoLine = getNodeInfo(nodeInfo);
        ClusterNodeInformation nodeInformation = parser.parse(nodeInfoLine,
                new redis.clients.jedis.HostAndPort(nodeInfo.getHost(), nodeInfo.getPort()));
        return nodeInformation;
    }


    static class ValueComparator implements Comparator<String> {
        Map<String, ArrayList<RawDataNode>> base;
        public ValueComparator(Map<String, ArrayList<RawDataNode>> base) {
            this.base = base;
        }
        public int compare(String a, String b) {
            if (base.get(a).size() >= base.get(b).size()) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    public static void main(String[] args) {
        RawDataNode rawDataNode1 = new RawDataNode("10.32.170.233",6379,"1");
        RawDataNode rawDataNode2 = new RawDataNode("10.32.146.76",6379,"1");
        RawDataNode rawDataNode3 = new RawDataNode("10.32.120.188",6379,"2");
        RawDataNode rawDataNode4 = new RawDataNode("10.32.170.232",6379,"2");
        RawDataNode rawDataNode5 = new RawDataNode("10.32.174.248",6379,"3");
        RawDataNode rawDataNode6 = new RawDataNode("10.32.171.226",6379,"3");
        RawDataNode rawDataNode7 = new RawDataNode("10.32.170.231",6379,"4");
        RawDataNode rawDataNode8 = new RawDataNode("10.32.146.75",6379,"4");
        RawDataNode rawDataNode9 = new RawDataNode("10.32.120.165",6379,"4");
        RawDataNode rawDataNode10 = new RawDataNode("10.32.170.230",6379,"4");
        RawDataNode rawDataNode11 = new RawDataNode("10.32.174.247",6379,"5");
        RawDataNode rawDataNode12 = new RawDataNode("10.32.171.225",6379,"5");
        RawDataNode rawDataNode13 = new RawDataNode("10.32.170.229",6379,"5");
        RawDataNode rawDataNode14 = new RawDataNode("10.32.146.74",6379,"5");
        RawDataNode rawDataNode15 = new RawDataNode("10.32.120.164",6379,"6");
        RawDataNode rawDataNode16 = new RawDataNode("10.32.170.228",6379,"7");
        RawDataNode rawDataNode17 = new RawDataNode("10.32.174.246",6379,"7");
        RawDataNode rawDataNode18 = new RawDataNode("10.32.171.224",6379,"8");

        List<RawDataNode> rawDataNodes = new ArrayList<RawDataNode>();
        rawDataNodes.add(rawDataNode1 );
        rawDataNodes.add(rawDataNode2 );
        rawDataNodes.add(rawDataNode3 );
        rawDataNodes.add(rawDataNode4 );
        rawDataNodes.add(rawDataNode5 );
        rawDataNodes.add(rawDataNode6 );
        rawDataNodes.add(rawDataNode7 );
        rawDataNodes.add(rawDataNode8 );
        rawDataNodes.add(rawDataNode9 );
        rawDataNodes.add(rawDataNode10 );
        rawDataNodes.add(rawDataNode11 );
        rawDataNodes.add(rawDataNode12 );
        rawDataNodes.add(rawDataNode13 );
        rawDataNodes.add(rawDataNode14 );
        rawDataNodes.add(rawDataNode15 );
        rawDataNodes.add(rawDataNode16 );
        rawDataNodes.add(rawDataNode17 );
        rawDataNodes.add(rawDataNode18 );

        createCluster(rawDataNodes,6,2);

    }
}
