package com.dianping.cache.dao;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import com.dianping.cache.entity.Server;

public interface ServerDao {

	List<Server>  findAll(int type);
	
	Server findByAddress(String address);
	
	void insert(Server server) throws DuplicateKeyException;
	
	void delete(String address);
	
	void update(Server server);

	void setDeleteType(String instanceId);

	void deleteByInstanceId(String instanceId);
}
