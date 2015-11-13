package com.dianping.squirrel.client.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class TimeMonitor {

	private static Logger logger = LoggerFactory.getLogger(TimeMonitor.class);

	private static final String timeRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"squirrel-client.monitor.time.range", "2,4,8,16,32,50");

	private static int[] timeRangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"squirrel-client.monitor.time.enable", true);

	private static final long timeMin = ConfigManagerLoader.getConfigManager().getIntValue(
			"squirrel-client.monitor.time.min", 5) * 1000000;

	private static class CacheTimeHolder {
		public static final TimeMonitor INSTANCE = new TimeMonitor();
	}

	public static TimeMonitor getInstance() {
		return CacheTimeHolder.INSTANCE;
	}

	private TimeMonitor() {
		if (enableMonitor) {
			init();
		}
	}

	private void init() {
		timeRangeArray = initRangeArray(timeRangeConfig);
		Cat.getProducer();
		Transaction t = Cat.newTransaction("System", "StoreClientStart");
		t.setStatus("0");
		t.complete();
	}

	private int[] initRangeArray(String rangeConfig) {
		String[] range = rangeConfig.split(",");
		int end = Integer.valueOf(range[range.length - 1]);
		int[] rangeArray = new int[end];
		int rangeIndex = 0;
		for (int i = 0; i < end; i++) {
			if (range.length > rangeIndex) {
				int value = Integer.valueOf(range[rangeIndex]);
				if (i >= value) {
					rangeIndex++;
				}
				rangeArray[i] = value;
			}
		}
		return rangeArray;
	}

	public void logTime(String cacheType, String category, String eventName, long time) {
		this.logTime(cacheType, category, eventName, time, null);
	}

	public void logTime(String cacheType, String category, String eventName, long time, String desc) {
		if (enableMonitor && time >= timeMin && cacheType != null && !"web".equalsIgnoreCase(cacheType)) {
			try {
				doLogTime(time, timeRangeArray, "Squirrel." + cacheType + "." + eventName + ".time", desc);
			} catch (Throwable t) {
				logger.warn("error while logging time:" + t.getMessage());
			}
		}
	}

	public void logTime(String cacheType, String category, String eventName, long time, long timeMinimum) {
		if (enableMonitor && time >= timeMinimum && cacheType != null && !"web".equalsIgnoreCase(cacheType)) {
			try {
				doLogTime(time, timeRangeArray, "Squirrel." + cacheType + "." + eventName + ".time", null);
			} catch (Throwable t) {
				logger.warn("error while logging time:" + t.getMessage());
			}
		}
	}

	private void doLogTime(long time, int[] rangeArray, String eventName, String desc) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = ">=" + rangeArray[rangeArray.length - 1];
			int t = (int) Math.ceil(time * 1d / 1000000);
			if (rangeArray.length > t) {
				value = "<=" + rangeArray[t];
			}
			Cat.getProducer().logEvent(eventName, value, Event.SUCCESS, desc != null ? (t + "#" + desc) : t + "");
		}
	}
}
