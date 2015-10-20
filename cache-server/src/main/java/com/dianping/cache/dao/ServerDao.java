package com.dianping.cache.dao;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;

import com.dianping.cache.entity.Server;

public interface ServerDao {

	List<Server>  findAll(int type);
	
	public Server findByAddress(String address);
	
	public void insert(Server server) throws DuplicateKeyException;
	
	public void delete(String address);
}
