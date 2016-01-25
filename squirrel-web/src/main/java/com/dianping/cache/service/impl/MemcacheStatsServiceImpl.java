package com.dianping.cache.service.impl;

import com.dianping.cache.dao.MemcachedStatsDao;
import com.dianping.cache.entity.MemcachedStats;
import com.dianping.cache.service.MemcachedStatsService;

import java.util.List;

public class MemcacheStatsServiceImpl implements MemcachedStatsService {

	private MemcachedStatsDao memcachedStatsDao;
	
	
	@Override
	public List<MemcachedStats> findByServer(String server) {
		// TODO Auto-generated method stub
		return memcachedStatsDao.findAll();
	}

	@Override
	public void insert(MemcachedStats stat) {
		// TODO Auto-generated method stub
		memcachedStatsDao.insert(stat);
	}
	
	@Override
	public List<MemcachedStats> findByServerWithInterval(String address,
														 long start, long end) {
		return memcachedStatsDao.findByServerWithInterval(address, start, end);
	}
	

	public MemcachedStatsDao getMemcachedStatsDao() {
		return memcachedStatsDao;
	}

	public void setMemcachedStatsDao(MemcachedStatsDao memcachedStatsDao) {
		this.memcachedStatsDao = memcachedStatsDao;
	}

	@Override
	public void delete(long timeBefore) {
		this.memcachedStatsDao.delete(timeBefore);
	}

	@Override
	public List<MemcachedStats> search(String sql) {
		return memcachedStatsDao.search(sql);
	}

}
