package com.dianping.cache.dao;

import java.util.List;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;

public interface ServerClusterDao {
	
	public void insert(ServerCluster serverCluster);
	
	public List<ServerCluster> findByServer(String server);
	
	public List<Server> findByCluster(String cluster);
	
	public void delete(ServerCluster serverCluster);
}
