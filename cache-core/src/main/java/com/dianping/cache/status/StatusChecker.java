package com.dianping.cache.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cache.monitor.QpsMonitor;

public class StatusChecker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StatusChecker.class);
	public static final int logPeriod = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.stat.log.period", 5000);
	public static final int logMinQps = ConfigManagerLoader.getConfigManager().getIntValue(
			"avatar-cache.stat.log.minqps", 0);
	public static final boolean logLocal = ConfigManagerLoader.getConfigManager().getBooleanValue(
	        "avatar-cache.stat.log.local", false);

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(logPeriod);
			} catch (InterruptedException e) {
			}
			if (StatusHolder.getCapacityBuckets() != null) {
				try {
					for (String key : StatusHolder.getCapacityBuckets().keySet()) {
						CapacityBucket bucket = StatusHolder.getCapacityBuckets().get(key);
						int qps = bucket.getRequestsInLastSecond();
						if (qps >= logMinQps) {
							QpsMonitor.getInstance().logQps("Cache." + key + ".qps", qps);
							if(logLocal) {
							    logger.info("Cache." + key + ".qps" + " : " + qps);
							}
						}
						bucket.resetRequestsInSecondCounter();
					}
				} catch (Throwable e) {
					logger.error("Check expired request in app statistics failed, detail[" + e.getMessage() + "].", e);
				}
			}
		}
	}

}
