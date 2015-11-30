package com.dianping.cache.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import com.dianping.cache.entity.Server;

public interface ServerService {
	List<Server> findAll();
	
	List<Server> findAllMemcachedServers();
	
	List<Server> findAllRedisServers();

	void insert(String address, String appId, String instanceId, int type, String hostIp) throws DuplicateKeyException;
	
	Server findByAddress(String address);
	
	void delete(String address);
	
	void update(Server server);

	void setDeleteType(String instanceId);

	void deleteByInstanceId(String instanceId);
}
