package com.dianping.cache.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import com.dianping.cache.entity.Server;

public interface ServerService {
	public List<Server> findAll();
	
	public List<Server> findAllMemcachedServers();
	
	public List<Server> findAllRedisServers();

	public void insert(String address, String appId,String instanceId, int type) throws DuplicateKeyException;
	
	public Server findByAddress(String address);
	
	public void delete(String address);
}
