package com.dianping.cache.scale.cluster.redis;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.ReshardRecord;
import org.apache.log4j.Logger;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisClusterException;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.support.spring.SpringLocator;
import com.dianping.cache.util.ParseServersUtil;
import redis.clients.jedis.exceptions.JedisConnectionException;
import static com.google.common.base.Preconditions.checkNotNull;

public class RedisManager {
	
	private static Logger logger = Logger.getLogger(RedisManager.class);

	public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
	public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
	public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
	public static final long CLUSTER_SLEEP_INTERVAL = 50;
	public static final int CLUSTER_DEFAULT_TIMEOUT = 300;
	public static final int CLUSTER_MIGRATE_NUM = 100;
	public static final int DEFAULT_CHECKPORT_TIMEOUT = 60000;
	public static final int CLUSTER_DEFAULT_DB = 0;
	public static final String UNIX_LINE_SEPARATOR = "\n";
	public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
	public static final String COLON_SEPARATOR = ":";

	private static final Map<String,RedisCluster> clusterCache = new TreeMap<String, RedisCluster>();

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static final Map<String,JedisPool> JEDIS_POOL_MAP = new HashMap<String, JedisPool>();

	static {
		refreshCache();
	}
	
	public static void create(List<RedisNode> nodes) {
		
	}
	
	public static void addMaster(String cluster,String master){
		RedisCluster rc = getRedisCluster(cluster);
		RedisServer rs = new RedisServer(master);
		joinCluster(rc, rs);
		refreshCache(cluster);
	}

	public static boolean addSlaveToMaster(String master, String slave) {
		try {
			RedisServer m = new RedisServer(master);
			RedisServer s = new RedisServer(slave);
			if (!checkPort(s)) {
				return false;
			}
			String masterNodeId = getNodeId(m);
			Jedis mJedis = new Jedis(m.getIp(), m.getPort());
			mJedis.clusterMeet(s.getIp(), s.getPort());
			waitForClusterReady(master);
			Jedis sJedis = new Jedis(s.getIp(), s.getPort());
			sJedis.clusterReplicate(masterNodeId);
			sJedis.close();
		} catch (Exception e) {
			logger.error("Add slave to " + master + " error ! Please check : "
					+ slave + "\n",e);
			return false;
		}
		return true;
	}

	public static boolean reshard(final ReshardPlan reshardPlan) {
		if (!checkClusterStatus(reshardPlan)) {
			return false;
		}
		RedisCluster redisCluster = new RedisCluster(reshardPlan.getSrcNode());
		List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
		for (final ReshardRecord reshardRecord : reshardRecordList) {
			while (true) {
				try {
					redisCluster.loadClusterInfo();
					RedisServer src = redisCluster.getServer(reshardRecord.getSrcNode());
					if(src == null)
                        continue;
					if(src.getSlotSize() == 0)
                        break;
					RedisServer des = redisCluster.getServer(reshardRecord.getDesNode());
					if (reshardRecord.getSlotsDone() < reshardRecord.getSlotsToMigrateCount()) {
                        int slot = src.getSlotList().get(0);
                        // set curr migrate slot in reshardplan
                        boolean result = migrate(src, des, slot);
                        if (result) {
                            //update resharRecord(slotsDone++)  reshardPlan(slotInMigrate = -1)
                            reshardRecord.setSlotsDone(reshardRecord.getSlotsDone()+1);
                        } else {
                            // re migrate this slot
                        }
                    } else {
                        break;
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * migrate slot from src node to des node,if migrate failed,
	 * fixed slot location and return false
	 * @param src
	 * @param des
	 * @param slot
     * @return
     */
	private static boolean migrate(RedisServer src,RedisServer des,int slot){
		Jedis srcNode = getResource(src.getIp(),src.getPort()).getResource();
		String srcNodeId = src.getId();
		Pipeline pipeline = srcNode.pipelined();
		Jedis destNode = getResource(des.getIp(),des.getPort()).getResource();
		String destNodeId = des.getId();

		/** migrate every slot from src node to dest node */
		destNode.clusterSetSlotImporting(slot, srcNodeId);
		srcNode.clusterSetSlotMigrating(slot, destNodeId);
		while (true) {
			try {
				List<String> keysInSlot = srcNode.clusterGetKeysInSlot(slot,CLUSTER_MIGRATE_NUM);
				if (keysInSlot == null || keysInSlot.isEmpty()) {
                    break;
                }
				for (String key : keysInSlot) {
                    pipeline.migrate(des.getIp(),des.getPort(), key,
                            CLUSTER_DEFAULT_DB,CLUSTER_DEFAULT_TIMEOUT);
                }
				pipeline.sync();
			} catch (Throwable e) {
				//// FIXME: 15/12/28
				srcNode.clusterSetSlotStable(slot);
				destNode.clusterSetSlotStable(slot);
				logger.warn("Migrate process may be down,Set Slot Stable .",e);
				getResource(src.getIp(),src.getPort()).returnResource(srcNode);
				getResource(des.getIp(),des.getPort()).returnResource(destNode);
				return false;
			}
		}
		/** wait for slots migration done */
		notifyAllNode(slot,destNodeId,des);
		waitForMigrationDone(src);
		waitForMigrationDone(des);
		getResource(src.getIp(),src.getPort()).returnResource(srcNode);
		getResource(des.getIp(),des.getPort()).returnResource(destNode);
		return true;
	}

	private static boolean checkClusterStatus(ReshardPlan reshardPlan){
		RedisCluster redisCluster = new RedisCluster(reshardPlan.getSrcNode());
		if(redisCluster.isMigrating() || reshardPlan.getStatus() == 400){
			return false;
		}
		List<ReshardRecord> reshardRecordList = reshardPlan.getReshardRecordList();
		for(ReshardRecord reshardRecord : reshardRecordList){
			RedisServer src = redisCluster.getServer(reshardRecord.getSrcNode());
			RedisServer srcInCluster = redisCluster.getServer(src.getAddress());
			if(!srcInCluster.getSlotList().containsAll(Slot.slotStringToList(reshardRecord.getSlotsToMigrate()))){
				return false;
			}
		}
		return true;
	}
	private static boolean checkPort(RedisServer redisServer){
		return checkPort(redisServer, DEFAULT_CHECKPORT_TIMEOUT);
	}

	private static boolean checkPort(RedisServer redisServer,long timeout){
		Jedis jedis = getResource(redisServer.getIp(),redisServer.getPort()).getResource();
		JedisConnectionException ex = null;
		long wait = 0;
		while (wait < timeout){
			try {
				if (jedis.ping().contains("PONG")){
					getResource(redisServer.getIp(),redisServer.getPort()).returnResource(jedis);
					return true;
				}
			}catch (JedisConnectionException e){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					logger.error("Wait for redis reponse interrupted",e1);
					return false;
				}
				wait += 1000;
				ex = e;
			}
		}
		logger.error("TimeOut for wait " + redisServer.getAddress() + " response .",ex);
		return false;
	}
	public static boolean removeServer(String cluster,String address) {
		refreshCache(cluster);
		RedisCluster rc = clusterCache.get(cluster);
		RedisServer nodeToDelete = rc.getServer(address);
		if(nodeToDelete == null){
			return false;
		}
		Jedis deleteNode = new Jedis(nodeToDelete.getIp(),nodeToDelete.getPort());
		String deleteNodeId = nodeToDelete.getId();
		List<RedisServer> allNodesOfCluster = rc.getAllAliveServer();
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
			deleteNode.close();
			refreshCache(cluster);
			return true;
		}
		//TODO   delete master node 
		deleteNode.close();
		refreshCache(cluster);
		return false;// master
	}

	public static boolean joinCluster(RedisCluster cluster, RedisServer server) {
		if(!checkPort(server)){
			return false;
		}
		RedisServer rsInCluster = cluster.getAllAliveServer().get(0);
		Jedis clusterJedis = new Jedis(rsInCluster.getIp(), rsInCluster.getPort());
		clusterJedis.clusterMeet(server.getIp(), server.getPort());
		List<RedisServer> clusterNodes = cluster.getAllAliveServer();
		boolean joinOk = false;
		long sleepTime = 0;
		while (!joinOk && sleepTime < CLUSTER_DEFAULT_TIMEOUT) {
			joinOk = true;
			for (RedisServer rs : clusterNodes) {
				if (!isNodeKnown(rs, server)) {
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

	public static String getNodeId(RedisServer server) {
		Jedis jedis = new Jedis(server.getIp(), server.getPort());
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

	private static boolean isNodeKnown(RedisServer srcNodeInfo,RedisServer targetNodeInfo) {
		Jedis srcNode = new Jedis(srcNodeInfo.getIp(), srcNodeInfo.getPort());
		String targetNodeId = getNodeId(targetNodeInfo);
		String[] clusterInfo = srcNode.clusterNodes()
				.split(UNIX_LINE_SEPARATOR);
		for (String infoLine : clusterInfo) {
			if (infoLine.contains(targetNodeId)) {
				srcNode.close();
				return true;
			}
		}
		srcNode.close();
		return false;
	}

	public static List<RedisServer> getAllNodesOfCluster(RedisServer server) {
		Jedis node = getResource(server.getIp(),server.getPort()).getResource();
		List<RedisServer> clusterNodeList = new ArrayList<RedisServer>();
		String[] clusterNodesOutput = node.clusterNodes().split(
				UNIX_LINE_SEPARATOR);
		for (String infoLine : clusterNodesOutput) {
			String[] hostAndPort = infoLine.split(" ")[1].split(":");
			RedisServer rs = new RedisServer(hostAndPort[0],
					Integer.valueOf(hostAndPort[1]));
			clusterNodeList.add(rs);
		}
		getResource(server.getIp(),server.getPort()).returnResource(node);
		return clusterNodeList;
	}

	private static String[] splitClusterInfo(String str) {
		if (str != null) {
			str = str.replaceAll("\r\n", "#");
			str = str.replaceAll("\n", "#");
			return str.split("#");
		}
		return null;
	}
	
	public static RedisCluster getRedisCluster(String cluster){
		if(clusterCache.containsKey(cluster)){
			return clusterCache.get(cluster);
		}
		refreshCache(cluster);
		return clusterCache.get(cluster);
	}
	
	public static RedisCluster refreshCache(String cluster){
		CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
		List<String> serverList = ParseServersUtil.parseRedisServers(cacheConfigurationService.find(cluster).getServers());
		RedisCluster rc = new RedisCluster(serverList);
		for (RedisNode node : rc.getNodes()) {
			node.getMaster().loadRedisInfo();
			if (node.getSlave() != null)
				node.getSlave().loadRedisInfo();
		}
		clusterCache.put(cluster, rc);
		return rc;
	}
	
    public static void waitForClusterReady(String clusterRandomAddress) {
        boolean clusterOk = false;
        while (!clusterOk) {
            clusterOk = true;
            List<RedisServer> serverList = getAllNodesOfCluster(new RedisServer(clusterRandomAddress));
            for (RedisServer rs : serverList) {
                Jedis jedis = new Jedis(rs.getIp(), rs.getPort());
                String clusterInfo = jedis.clusterInfo();
                String firstLine = clusterInfo.split(UNIX_LINE_SEPARATOR)[0];
                String[] firstLineArr = firstLine.trim().split(COLON_SEPARATOR);
                if (firstLineArr[0].contains("cluster_state") &&
                        firstLineArr[1].contains("ok")) {
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

	private static void waitForMigrationDone(final RedisServer server) {
		checkNotNull(server, "nodesInfo is null.");

		Jedis node = getResource(server.getIp(),server.getPort()).getResource();
		String[] clusterNodesInfo;

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
		getResource(server.getIp(),server.getPort()).returnResource(node);
	}


	private static void notifyAllNode(int slot,String destNodeId,RedisServer redisServer){
		List<RedisServer> servers = getAllNodesOfCluster(redisServer);
		for(RedisServer server : servers){
			Jedis jedis = getResource(server.getIp(),server.getPort()).getResource();
			jedis.clusterSetSlotNode(slot,destNodeId);
			getResource(server.getIp(),server.getPort()).returnResource(jedis);
		}
	}
	private static void refreshCache(){
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
				List<CacheConfiguration> configurations = cacheConfigurationService.findAll();
				Map<String,RedisCluster> tempClusterCache = new TreeMap<String, RedisCluster>();
				for (CacheConfiguration configuration : configurations) {
					if (configuration.getCacheKey().startsWith("redis") &&
							"".equals(configuration.getSwimlane())) {
						String url = configuration.getServers();
						List<String> servers = ParseServersUtil.parseRedisServers(url);
						RedisCluster cluster = new RedisCluster(servers);
						for (RedisNode node : cluster.getNodes()) {
							node.getMaster().loadRedisInfo();
							if (node.getSlave() != null)
								node.getSlave().loadRedisInfo();
						}
						tempClusterCache.put(configuration.getCacheKey(), cluster);
					}
				}
				clusterCache.clear();
				clusterCache.putAll(tempClusterCache);
			}
		},15,30,TimeUnit.SECONDS);
	}

	private static JedisPool getResource(String ip, int port){
		String address = ip + ":" + port;
		JedisPool jedisPool = JEDIS_POOL_MAP.get(address);
		if(jedisPool == null){
			synchronized (JEDIS_POOL_MAP){
				if(jedisPool == null){
					jedisPool = new JedisPool(ip,port);
					JEDIS_POOL_MAP.put(address,jedisPool);
				}
			}
		}
		return jedisPool;
	}
	public static Map<String,RedisCluster> getClusterCache() {
		return clusterCache;
	}
}
