package com.dianping.cache.service.impl;

import com.dianping.cache.dao.RedisStatsDao;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.service.RedisService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedisServiceImpl implements RedisService {
	
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

	@Override
	public List<RedisStats> search(String sql) {
		return redisStatsDao.search(sql);
	}

	@Override
	public List<RedisStats> findPeriodicStats(String address, long end, int period,int count) {
		long interval =TimeUnit.SECONDS.convert(period, TimeUnit.DAYS);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND,59);
		calendar.set(Calendar.MILLISECOND, 0);
		long begin = calendar.getTimeInMillis()/1000 - count*interval;
		List<RedisStats> result = new ArrayList<RedisStats>();
		while(begin < end){
			List<RedisStats> statses = redisStatsDao.findByServerWithInterval(address,begin-300,begin+300);
			if(statses != null && statses.size() > 0){
				result.add(statses.get(statses.size()/2));
			}else {
				result.add(null);
			}
			begin+=interval;
		}
		List<RedisStats> statses = redisStatsDao.findByServerWithInterval(address,end-600,end+60);
		if(statses != null && statses.size() > 0){
			result.add(statses.get(statses.size()/2));
		}else {
			result.add(null);
		}
		return result;
	}

	public RedisStatsDao getRedisStatsDao() {
		return redisStatsDao;
	}

	public void setRedisStatsDao(RedisStatsDao redisStatsDao) {
		this.redisStatsDao = redisStatsDao;
	}

}
