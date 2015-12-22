package com.dianping.cache.timedtask;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dianping.cache.service.RedisStatsService;
import com.dianping.cache.support.spring.SpringLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManager;
import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.OperationLogService;
import com.dianping.cache.service.ServerStatsService;

public class TimedCleanData {
	private Logger logger = LoggerFactory.getLogger(TimedCleanData.class);
	private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
	
	private static final String OPERATIONLOG_CLEAN_TIME = "avatar-cache.timeclean.operationlog";
	private static final int DEFAULT_OPERATIONLOG_CLEAN_TIME = 365;
	
	private static final String STATS_CLEAN_TIME = "avatar-cache.timeclean.stats";
	private static final int DEFAULT_STATS_CLEAN_TIMER = 15;
	
	private static int logtime = configManager.getIntValue(OPERATIONLOG_CLEAN_TIME,DEFAULT_OPERATIONLOG_CLEAN_TIME);
	private static int statstime = configManager.getIntValue(STATS_CLEAN_TIME,DEFAULT_STATS_CLEAN_TIMER);
	
	private ScheduledExecutorService scheduled  = Executors.newSingleThreadScheduledExecutor();
	
	private ServerStatsService serverStatsService;
	
	private MemcacheStatsService memcacheStatsService;
	
	private OperationLogService operationLogService;

	private RedisStatsService redisStatsService;
	
	public TimedCleanData(){
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
				cleanOperationLog();
				cleanServerStats();
				cleanMemcachedStats();
				cleanRedisStats();
			}
			
		},20 , 24 * 60 * 60, TimeUnit.SECONDS);
	}
	
	private void init(){
		serverStatsService = SpringLocator.getBean("serverStatsService");
		memcacheStatsService = SpringLocator.getBean("memcacheStatsService");
		operationLogService = SpringLocator.getBean("operationLogService");
		redisStatsService = SpringLocator.getBean("redisStatsService");
	}
	/**
	 * 清理一个月前的日志
	 */
	private void cleanServerStats(){
		logger.info("start to clean ServerStats !");
		long timeBefore = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(statstime * 24 * 60, TimeUnit.MINUTES))/1000;
		serverStatsService.delete(timeBefore);
		logger.info("ServerStats clear !");
	}
	
	private void cleanMemcachedStats(){
		logger.info("start to clean MemcachedStats !");
		long timeBefore = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(statstime * 24 * 60, TimeUnit.MINUTES))/1000;
		memcacheStatsService.delete(timeBefore);
		logger.info("MemcachedStats clear!");
	}
	
	private void cleanOperationLog(){
		logger.info("start to clean OperationLog !");
		Date date =  new Date();
		date.setTime(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(logtime * 24 * 60, TimeUnit.MINUTES));
		operationLogService.delete(date);
		logger.info("OperationLog clear!");
	}

	private void cleanRedisStats(){
		logger.info("start to clean RedisStats !");
		long timeBefore = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(statstime * 24 * 60, TimeUnit.MINUTES))/1000;
		redisStatsService.delete(timeBefore);
		logger.info("RedisStats clear!");
	}
}
