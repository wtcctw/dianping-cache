package com.dianping.cache.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cache.dao.ServerDao;
import com.dianping.cache.entity.Server;
import com.dianping.cache.service.ServerService;

public class ServerServiceImpl implements ServerService{
	
	private ServerDao serverDao;
	
	@Override
	public List<Server> findAll() {
		List<Server> result = new ArrayList<Server>();
		result.addAll(serverDao.findAll(0));
		result.addAll(serverDao.findAll(1));
		return result;
	}

	@Override
	public void insert(String address, String appId, String instanceId,int type) {
		Server server = new Server();
		server.setAddress(address);
		server.setInstanceId(instanceId);
		server.setAppId(appId);
		server.setType(type);
		serverDao.insert(server);
	}
	
	@Override
	public Server findByAddress(String address) {
		return serverDao.findByAddress(address);
	}
	
	@Override
	public void delete(String address) {
		serverDao.delete(address);
	}
	
	public ServerDao getServerDao() {
		return serverDao;
	}
	
	public void setServerDao(ServerDao serverDao) {
		this.serverDao = serverDao;
	}

	@Override
	public List<Server> findAllMemcachedServers() {
		return serverDao.findAll(0);
	}

	@Override
	public List<Server> findAllRedisServers() {
		return serverDao.findAll(1);
	}



}
