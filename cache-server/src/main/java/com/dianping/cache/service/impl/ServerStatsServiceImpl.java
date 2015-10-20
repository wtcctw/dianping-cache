package com.dianping.cache.service.impl;

import java.util.List;

import com.dianping.cache.dao.ServerStatsDao;
import com.dianping.cache.entity.ServerStats;
import com.dianping.cache.service.ServerStatsService;

public class ServerStatsServiceImpl implements ServerStatsService{
	
	private ServerStatsDao  serverStatsDao;
	
	@Override
	public List<ServerStats> findByServer(String server) {
		return serverStatsDao.findByServer(server);
	}

	@Override
	public void insert(ServerStats stat) {
		serverStatsDao.insert(stat);
	}

	@Override
	public List<ServerStats> findByServerWithInterval(String address,
			long start, long end) {
		return serverStatsDao.findByServerWithInterval(address, start, end);
	}

	public ServerStatsDao getServerStatsDao() {
		return serverStatsDao;
	}

	public void setServerStatsDao(ServerStatsDao serverStatsDao) {
		this.serverStatsDao = serverStatsDao;
	}

	@Override
	public void delete(long timeBefore) {
		serverStatsDao.delete(timeBefore);
	}

}
