package com.dianping.cache.service.impl;

import java.util.List;

import com.dianping.cache.dao.RedisStatsDao;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.service.RedisStatsService;

public class RedisStatsServiceImpl implements RedisStatsService{
	
	private RedisStatsDao redisStatsDao;

	@Override
	public List<RedisStats> findByServer(String server) {
		// TODO Auto-generated method stub
		return redisStatsDao.findByServer(server);
	}

	@Override
	public void insert(RedisStats stat) {
		// TODO Auto-generated method stub
		redisStatsDao.insert(stat);
	}

	@Override
	public List<RedisStats> findByServerWithInterval(String address,
			long start, long end) {
		// TODO Auto-generated method stub
		return redisStatsDao.findByServerWithInterval(address, start, end);
	}

	@Override
	public void delete(long timeBefore) {
		// TODO Auto-generated method stub
		redisStatsDao.delete(timeBefore);
	}

	public RedisStatsDao getRedisStatsDao() {
		return redisStatsDao;
	}

	public void setRedisStatsDao(RedisStatsDao redisStatsDao) {
		this.redisStatsDao = redisStatsDao;
	}

}