package com.dianping.cache.service;

import java.util.List;

import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;

public interface ServerClusterService {
	
	public void insert(String server,String cluster);
	
	public List<ServerCluster> findByServer(String server);
	
	public List<Server> findByCluster(String cluster);
	
	public void delete(String server,String cluster);
}
