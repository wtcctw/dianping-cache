package com.dianping.cache.monitor.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.util.NetUtil;

public abstract class AbstractStatsDataStorage {
	
	private static final int DEFAULT_INTERVAL = 30;
	
	private static final ArrayList<String> IPLIST = new ArrayList<String>(){{
		add("10.3.8.62");//线上
		add("10.2.8.147");//ppe
		add("192.168.211.117");//beta
		add("10.128.120.31");//my host
	}};
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected static ScheduledExecutorService scheduled  = Executors.newScheduledThreadPool(2);
	
	private int storagerInterval = DEFAULT_INTERVAL;
	
	protected void init(){
		
	}

	public int getStoragerInterval() {
		return storagerInterval;
	}

	public void setStoragerInterval(int storagerInterval) {
		this.storagerInterval = storagerInterval;
	}
	
	public boolean isMaster(){
		boolean isMaster = false;
		try {
			List<String> ip= NetUtil.getAllLocalIp();
			ip.retainAll(IPLIST);
			if(ip.size() > 0)
				isMaster = true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isMaster;
	}
	
}
