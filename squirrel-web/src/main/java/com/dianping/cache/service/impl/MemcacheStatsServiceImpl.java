package com.dianping.cache.service.impl;

import com.dianping.cache.dao.MemcacheStatsDao;
import com.dianping.cache.entity.MemcacheStats;
import com.dianping.cache.service.MemcacheStatsService;

import java.util.List;

public class MemcacheStatsServiceImpl implements MemcacheStatsService{

	private MemcacheStatsDao memcacheStatsDao;
	
	
	@Override
	public List<MemcacheStats> findByServer(String server) {
		// TODO Auto-generated method stub
		return memcacheStatsDao.findAll();
	}

	@Override
	public void insert(MemcacheStats stat) {
		// TODO Auto-generated method stub
		memcacheStatsDao.insert(stat);
	}
	
	@Override
	public List<MemcacheStats> findByServerWithInterval(String address,
			long start, long end) {
		return memcacheStatsDao.findByServerWithInterval(address, start, end);
	}
	

	public MemcacheStatsDao getMemcacheStatsDao() {
		return memcacheStatsDao;
	}

	public void setMemcacheStatsDao(MemcacheStatsDao memcacheStatsDao) {
		this.memcacheStatsDao = memcacheStatsDao;
	}

	@Override
	public void delete(long timeBefore) {
		this.memcacheStatsDao.delete(timeBefore);
	}

	@Override
	public List<MemcacheStats> search(String sql) {
		return memcacheStatsDao.search(sql);
	}

}
