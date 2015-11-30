package com.dianping.cache.service.impl;

import java.util.List;

import com.dianping.cache.dao.ServerClusterDao;
import com.dianping.cache.dao.ServerDao;
import com.dianping.cache.entity.Server;
import com.dianping.cache.entity.ServerCluster;
import com.dianping.cache.service.ServerClusterService;

public class ServerClusterServiceImpl implements ServerClusterService {

	private ServerClusterDao serverClusterDao;
	
	private ServerDao serverDao;
	@Override
	public void insert(String address, String cluster) {
		Server server = serverDao.findByAddress(address);
		ServerCluster serverCluster = new ServerCluster();
		serverCluster.setServerId(server.getId());
		serverCluster.setCluster(cluster);
		serverClusterDao.insert(serverCluster);
	}
	
	@Override
	public void delete(String address, String cluster) {
		Server server = serverDao.findByAddress(address);
		ServerCluster serverCluster = new ServerCluster();
		serverCluster.setServerId(server.getId());
		serverCluster.setCluster(cluster);
		serverClusterDao.delete(serverCluster);
	}

	@Override
	public void deleteServer(String address) {
		//Server server = serverDao.findByAddress(address);
		serverClusterDao.deleteServer(address);
	}

	@Override
	public List<ServerCluster> findByServer(String server) {
		return serverClusterDao.findByServer(server);
	}

	@Override
	public List<Server> findByCluster(String cluster) {
		return serverClusterDao.findByCluster(cluster);
	}

	public ServerClusterDao getServerClusterDao() {
		return serverClusterDao;
	}

	public void setServerClusterDao(ServerClusterDao serverClusterDao) {
		this.serverClusterDao = serverClusterDao;
	}

	public ServerDao getServerDao() {
		return serverDao;
	}

	public void setServerDao(ServerDao serverDao) {
		this.serverDao = serverDao;
	}


}
