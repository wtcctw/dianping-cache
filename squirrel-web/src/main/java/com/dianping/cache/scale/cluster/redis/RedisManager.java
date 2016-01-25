package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.ReshardRecord;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.util.ConfigUrlUtil;
import com.dianping.cache.util.SpringLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisClusterException;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class RedisManager {
	
	private static Logger logger = LoggerFactory.getLogger(RedisManager.class);

	public static final String SLOT_IN_TRANSITION_IDENTIFIER = "[";
	public static final String SLOT_IMPORTING_IDENTIFIER = "--<--";
	public static final String SLOT_MIGRATING_IDENTIFIER = "-->--";
	public static final long CLUSTER_SLEEP_INTERVAL = 50;
	public static final int CLUSTER_DEFAULT_TIMEOUT = 20000;
	public static final int CLUSTER_MIGRATE_NUM = 10;
	public static final int DEFAULT_CHECKPORT_TIMEOUT = 60000;
	public static final int CLUSTER_DEFAULT_DB = 0;
	public static final String UNIX_LINE_SEPARATOR = "\n";
	public static final String WINDOWS_LINE_SEPARATOR = "\r\n";
	public static final String COLON_SEPARATOR = ":";

	private static Map<String,RedisCluster> clusterCache = new TreeMap<String, RedisCluster>();

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static final Map<String,JedisPool> JEDIS_POOL_MAP = new HashMap<String, JedisPool>();

	static {
		refreshCache();
	}
	
	public static void create(List<RedisNode> nodes) {
		
	}
	
	public static void addMaster(String cluster,String master){
		RedisCluster rc = getRedisCluster(cluster);
		RedisServer rs = rc.getServer(master);
		if(rs == null){
			return;
		}
		joinCluster(rc, rs);
		refreshCache(cluster);
	}

	public static boolean addSlaveToMaster(String master, String slave) {
		try {
			RedisServer m = getServerInCluster(master);
			RedisServer s = getServerInCluster(slave);
			if (m == null || s == null || !checkPort(s)) {
				return false;
			}
			String masterNodeId = getNodeId(m);
			Jedis mJedis = JedisAuthWapper.getJedis(m);
			mJedis.clusterMeet(s.getIp(), s.getPort());
			waitForClusterReady(master);
			Jedis sJedis = JedisAuthWapper.getJedis(s);
			sJedis.clusterReplicate(masterNodeId);

			JedisAuthWapper.returnResource(mJedis);
			JedisAuthWapper.returnResource(sJedis);

		} catch (Exception e) {
			logger.error("Add slave to " + master + " error ! Please check : "
					+ slave + "\n",e);
			return false;
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
	public static boolean migrate(RedisServer src,RedisServer des,int slot){
		return  migrate(src,des,slot,false);
	}

	public static boolean migrate(RedisServer src,RedisServer des,int slot,boolean open){
		Jedis srcNode = JedisAuthWapper.getJedis(src);
		String srcNodeId = src.getId();
		Pipeline pipeline = srcNode.pipelined();
		Jedis destNode = JedisAuthWapper.getJedis(des);
		String destNodeId = des.getId();
		int timeout = CLUSTER_DEFAULT_TIMEOUT;
		/** migrate every slot from src node to dest node */
		if(!open){
			destNode.clusterSetSlotImporting(slot, srcNodeId);
			srcNode.clusterSetSlotMigrating(slot, destNodeId);
		}
		while (true) {
			try {
				List<String> keysInSlot = srcNode.clusterGetKeysInSlot(slot,CLUSTER_MIGRATE_NUM);
				if (keysInSlot == null || keysInSlot.isEmpty()) {
                    break;
                }
				Map<String,Response<String>> responseMap = new HashMap<String, Response<String>>(keysInSlot.size());
				for (String key : keysInSlot) {
                    Response<String> str = pipeline.migrate(des.getIp(),des.getPort(), key,
                            CLUSTER_DEFAULT_DB,timeout);
					responseMap.put(key,str);
                }
				pipeline.sync();
				for(Map.Entry<String,Response<String>> item : responseMap.entrySet()){
					//try {
						item.getValue().get();
//					}catch (Exception e){
//						if(e.toString().contains("BUSYKEY")){
//							logger.error("BUSYKEY key:{}",item.getKey());
//							//srcNode.del(item.getKey());
//							throw e;
//						}else if(e.toString().contains("IOERR")){
//							timeout *= 2;
//							if(timeout > 60000){
//								throw e;
//							}
//						}
//					}
				}
			} catch (Throwable e) {
				logger.warn("Migrate process may be down.",e);
				JedisAuthWapper.returnResource(srcNode);
				JedisAuthWapper.returnResource(destNode);
				return false;
			}
		}
		/** wait for slots migration done */
		notifyAllNode(slot,destNodeId,des);
		waitForMigrationDone(src);
		waitForMigrationDone(des);
		JedisAuthWapper.returnResource(srcNode);
		JedisAuthWapper.returnResource(destNode);
		return true;
	}

	public static boolean checkClusterStatus(ReshardPlan reshardPlan){
		RedisCluster redisCluster = new RedisCluster(reshardPlan.getCluster(),reshardPlan.getSrcNode());
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
		Jedis jedis = JedisAuthWapper.getJedis(redisServer);
		JedisConnectionException ex = null;
		long wait = 0;
		while (wait < timeout){
			try {
				if (jedis.ping().contains("PONG")){
					//JedisAuthWapper.returnResource(jedis);
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
			}finally {
				JedisAuthWapper.returnResource(jedis);
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
		Jedis deleteNode = JedisAuthWapper.getJedis(nodeToDelete);
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
					Jedis conn = JedisAuthWapper.getJedis(node);
					conn.clusterForget(deleteNodeId);
					JedisAuthWapper.returnResource(conn);
				}
			}
			JedisAuthWapper.returnResource(deleteNode);
			refreshCache(cluster);
			return true;
		}
		//TODO   delete master node 
		JedisAuthWapper.returnResource(deleteNode);
		refreshCache(cluster);
		return false;// master
	}

	public static boolean joinCluster(RedisCluster cluster, RedisServer server) {
		if(!checkPort(server)){
			return false;
		}
		RedisServer rsInCluster = cluster.getAllAliveServer().get(0);
		Jedis clusterJedis = JedisAuthWapper.getJedis(rsInCluster);
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
		JedisAuthWapper.returnResource(clusterJedis);
		return joinOk;
	}

	public static String getNodeId(RedisServer server) {
		Jedis jedis = JedisAuthWapper.getJedis(server);
		String[] clusterInfo = splitClusterInfo(jedis.clusterNodes());
		for (String lineInfo : clusterInfo) {
			if (lineInfo.contains("myself")) {
				JedisAuthWapper.returnResource(jedis);
				return lineInfo.split(" ")[0];
			}
		}
		JedisAuthWapper.returnResource(jedis);
		return null;
	}

	private static boolean isNodeKnown(RedisServer srcNodeInfo,RedisServer targetNodeInfo) {
		Jedis srcNode = JedisAuthWapper.getJedis(srcNodeInfo);
		String targetNodeId = getNodeId(targetNodeInfo);
		String[] clusterInfo = srcNode.clusterNodes()
				.split(UNIX_LINE_SEPARATOR);
		for (String infoLine : clusterInfo) {
			if (infoLine.contains(targetNodeId)) {
				JedisAuthWapper.returnResource(srcNode);
				return true;
			}
		}
		JedisAuthWapper.returnResource(srcNode);
		return false;
	}

	public static List<RedisServer> getAllNodesOfCluster(RedisServer server) {
		Jedis node = JedisAuthWapper.getJedis(server);
		List<RedisServer> clusterNodeList = new ArrayList<RedisServer>();
		String[] clusterNodesOutput = node.clusterNodes().split(
				UNIX_LINE_SEPARATOR);
		for (String infoLine : clusterNodesOutput) {
			if(!infoLine.contains("fail")){
				String[] hostAndPort = infoLine.split(" ")[1].split(":");
				RedisServer rs = new RedisServer(hostAndPort[0],
						Integer.valueOf(hostAndPort[1]));
				clusterNodeList.add(rs);
			}
		}
		JedisAuthWapper.returnResource(node);
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
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		List<String> serverList = ConfigUrlUtil.serverList(config);
		RedisCluster rc = new RedisCluster(config.getCacheKey(),serverList,ConfigUrlUtil.getProperty(config,"password"));
		for (RedisNode node : rc.getNodes()) {
			node.getMaster().loadRedisInfo();
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

		Jedis node = JedisAuthWapper.getJedis(server);
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
		JedisAuthWapper.returnResource(node);
	}


	private static void notifyAllNode(int slot,String destNodeId,RedisServer redisServer){
		List<RedisServer> servers = getAllNodesOfCluster(redisServer);
		for(RedisServer server : servers){
			Jedis jedis = JedisAuthWapper.getJedis(server);
			jedis.clusterSetSlotNode(slot,destNodeId);
			JedisAuthWapper.returnResource(jedis);
		}
	}

	public static void fixOpenSlot(String cluster,int slot){
		RedisCluster redisCluster = getRedisCluster(cluster);
		List<RedisServer> importing = new ArrayList<RedisServer>();
		List<RedisServer> migrating = new ArrayList<RedisServer>();
		for(RedisServer redisServer : redisCluster.getAllAliveServer()){
			if(redisServer.isMaster()){
				if(redisServer.getMigrating() && redisServer.getOpenSlot() == slot){
					migrating.add(redisServer);
				}else if(redisServer.getImporting() && redisServer.getOpenSlot() == slot){
					importing.add(redisServer);
				}
			}
		}

		if(importing.size() == 1 && migrating.size() == 1){
			migrate(importing.get(0),migrating.get(0),slot,true);
		}
	}

	private static void refreshCache() {
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				CacheConfigurationService cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
				List<CacheConfiguration> configurations = cacheConfigurationService.findAll();
				Map<String, RedisCluster> tempClusterCache = new TreeMap<String, RedisCluster>();
				for (CacheConfiguration configuration : configurations) {
					if (configuration.getCacheKey().startsWith("redis") &&
							"".equals(configuration.getSwimlane())) {
						List<String> servers = ConfigUrlUtil.serverList(configuration);
						RedisCluster cluster = new RedisCluster(configuration.getCacheKey(), servers, ConfigUrlUtil.getProperty(configuration, "password"));
						for (RedisNode node : cluster.getNodes()) {
							node.getMaster().loadRedisInfo();
							if(node.getSlave() != null){
								node.getSlave().loadRedisInfo();
							}
						}
						tempClusterCache.put(configuration.getCacheKey(), cluster);
					}
				}
				clusterCache = tempClusterCache;
			}
		}, 3, 30, TimeUnit.SECONDS);
	}

	private static boolean isMigating(RedisServer redisServer){
		Jedis jedis = new Jedis(redisServer.getIp(),redisServer.getPort());
		String nodesStr = jedis.clusterNodes();
		String[] nodes = nodesStr.split("\n");
		for(String node : nodes){
			if(node.contains("myself") && node.contains(SLOT_MIGRATING_IDENTIFIER)){
				return true;
			}
		}
		return false;
	}

	private static boolean isMigrating(String address){
		RedisServer redisServer = new RedisServer(address);
		return isMigating(redisServer);
	}


	public static Map<String,RedisCluster> getClusterCache() {
		return clusterCache;
	}

	public static List<RedisServer> getServerInClusterCache(String cluster , List<String> addressList){
		if(cluster == null)
			return null;
		RedisCluster redisCluster = RedisManager.refreshCache(cluster);
		if(redisCluster == null){
			return null;
		}
		List<RedisServer> servers = new ArrayList<RedisServer>();
		for(String address : addressList){
			RedisServer server = redisCluster.getServer(address);
			if(server.isSlave()){
				return null;
			}
			servers.add(server);
		}
		return servers;
	}

	public static RedisServer getServerInCluster(String address){
		return getServerInCluster(null,address);
	}

	public static RedisServer getServerInCluster(String cluster,String address){
		RedisServer server = null;
		if(cluster != null){
			RedisCluster redisCluster = RedisManager.getRedisCluster(cluster);
			server = redisCluster.getServer(address);
		}else{
			for(Map.Entry<String,RedisCluster> clusterEntry : RedisManager.getClusterCache().entrySet()){
				if(clusterEntry.getValue().getServer(address) != null){
					server = clusterEntry.getValue().getServer(address);
				}
			}
		}
		if(server ==null)
			server = new RedisServer(address);
		return server;
	}

	public static boolean failover(String cluster, String slaveAddress) {
		try {
			RedisServer server = getServerInCluster(cluster,slaveAddress);
			Jedis jedis = JedisAuthWapper.getJedis(server);
			jedis.clusterFailover();
			JedisAuthWapper.returnResource(jedis);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}
}
