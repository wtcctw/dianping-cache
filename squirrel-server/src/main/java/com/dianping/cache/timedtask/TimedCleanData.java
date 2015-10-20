package com.dianping.cache.timedtask;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.OperationLogService;
import com.dianping.cache.service.ServerStatsService;
import com.dianping.combiz.spring.context.SpringLocator;

public class TimedCleanData {
	private Logger logger = LoggerFactory.getLogger(TimedCleanData.class);
	
	private ScheduledExecutorService scheduled  = Executors.newSingleThreadScheduledExecutor();
	
	private ServerStatsService serverStatsService;
	
	private MemcacheStatsService memcacheStatsService;
	
	private OperationLogService operationLogService;
	
	public TimedCleanData(){
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				cleanOperationLog();
				cleanServerStats();
				cleanMemcachedStats();
			}
			
		},0 , 24 * 60 * 60, TimeUnit.SECONDS);
	}
	
	private void init(){
		serverStatsService = SpringLocator.getBean("serverStatsService");
		memcacheStatsService = SpringLocator.getBean("memcacheStatsService");
		operationLogService = SpringLocator.getBean("operationLogService");
	}
	/**
	 * 清理一个月前的日志
	 */
	private void cleanServerStats(){
		logger.info("start to clean ServerStats !");
		long timeBefore = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(30 * 24 * 60, TimeUnit.MINUTES))/1000;
		serverStatsService.delete(timeBefore);
		logger.info("ServerStats clear !");
	}
	
	private void cleanMemcachedStats(){
		logger.info("start to clean MemcachedStats !");
		long timeBefore = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(30 * 24 * 60, TimeUnit.MINUTES))/1000;
		memcacheStatsService.delete(timeBefore);
		logger.info("MemcachedStats clear!");
	}
	
	private void cleanOperationLog(){
		logger.info("start to clean OperationLog !");
		Date date =  new Date();
		date.setTime(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(30 * 24 * 60, TimeUnit.MINUTES));
		operationLogService.delete(date);
		logger.info("OperationLog clear!");
	}
}
