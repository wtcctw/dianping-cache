package com.dianping.cache.dao;

import java.util.List;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;

public interface ServerClusterDao {
	
	 void insert(ServerCluster serverCluster);
	
	 List<ServerCluster> findByServer(String server);
	
	 List<Server> findByCluster(String cluster);
	
	void delete(ServerCluster serverCluster);

	void deleteServer(String address);
}
