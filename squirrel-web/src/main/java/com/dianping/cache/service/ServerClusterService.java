package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;

public interface ServerClusterService {
	
	void insert(String server, String cluster);
	
	List<ServerCluster> findByServer(String server);
	
	List<Server> findByCluster(String cluster);
	
	void delete(String server, String cluster);

	void deleteServer(String address);
}
