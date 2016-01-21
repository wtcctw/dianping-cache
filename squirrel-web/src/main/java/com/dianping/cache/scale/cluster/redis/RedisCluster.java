package com.dianping.cache.scale.cluster.redis;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.cluster.Cluster;
import com.dianping.cache.scale.cluster.Server;
import com.dianping.cache.util.ConfigUrlUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

@JsonIgnoreProperties({"password"})
public class RedisCluster implements Cluster<RedisNode>{
	
	private static Logger logger = LoggerFactory.getLogger(RedisCluster.class);

	private String clusterName;

	private String password;

    private List<String> serverList;

    private List<RedisNode> nodes = new ArrayList<RedisNode>();

	private List<RedisServer> failedServers = new ArrayList<RedisServer>();

    public RedisCluster(List<String> serverList) {
        this.serverList = serverList;
        loadClusterInfo();
    }
	public RedisCluster(String clusterName,List<String> serverList){
		this(clusterName,serverList,ConfigUrlUtil.getPassword(clusterName));
	}

	public RedisCluster(CacheConfiguration configuration){
		this.clusterName = configuration.getCacheKey();
		this.serverList = ConfigUrlUtil.serverList(configuration);
		this.password = ConfigUrlUtil.getProperty(configuration,"password");
		loadClusterInfo();
	}

	public RedisCluster(String clusterName,List<String> serverList,String password){
		this.clusterName = clusterName;
		this.password = password;
		this.serverList = serverList;
		loadClusterInfo();
	}


	@Override
	public List<RedisNode> getNodes() {
		if(nodes == null && serverList != null){
			loadClusterInfo();
		}
		return nodes;
	}
	
	public RedisServer getServer(String address){
		RedisServer server = new RedisServer(address);
		if(nodes != null){
			for(RedisNode node : nodes){
				if(server.equals(node.getMaster())){
					return node.getMaster();
				}else if(server.equals(node.getSlave())){
					return node.getSlave();
				}
			}
		}
		return null;
	}
	
	public List<RedisServer> getAllAliveServer(){
		loadClusterInfo();
		List<RedisServer> servers = new ArrayList<RedisServer>();
		for(RedisNode node : nodes){
			if(node.getMaster().isAlive())
				servers.add(node.getMaster());
			if(node.getSlave() != null && node.getSlave().isAlive())
				servers.add(node.getSlave());
		}
		return servers;
	}
	
	public void loadClusterInfo() {
		if (serverList == null || serverList.size() == 0) {
			throw new ScaleException("server list is empty");
		}
		int retry = 0;
		// 目前没有考虑集群割裂的情况 仅仅是考虑有一个节点可能链接不上
		for (String address : serverList) {
			Jedis jedis = null;
			Server server = new Server(address);
			try {
				retry++;
				jedis = new Jedis(server.getIp(), server.getPort());
				if(password != null && !"".equals(password)) {
					jedis.auth(password);
				}
				String clusterInfo = jedis.clusterNodes();
				nodes = parseClusterInfo(clusterInfo);
				break;
			} catch (Exception e) {
				if(retry >= 3){
					logger.error("can't load cluster info,",e);
					break;
				}
				continue;
			} finally{
				if(jedis != null){
					try {
						jedis.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	
    private List<RedisNode> parseClusterInfo(String clusterInfo) {
        List<RedisServer> servers = new ArrayList<RedisServer>();
        for(String serverInfo : clusterInfo.split("\n")) {
            RedisServer server = parseServerInfo(serverInfo);
			server.setRedisCluster(this);
			if(server.isFail() || server.isPartialFail()){
				failedServers.add(server);
			}else{
				servers.add(server);
			}
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

	public List<RedisServer> getFailedServers() {
		return failedServers;
	}

	public void setFailedServers(List<RedisServer> failedServers) {
		this.failedServers = failedServers;
	}

	public boolean isMigrating(){
		for (RedisNode redisNode : nodes){
			if(redisNode.getMaster().getMigrating())
				return true;
		}
		return false;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getPassword() {
		return password;
	}
}
