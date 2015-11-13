package com.dianping.squirrel.client.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class HitRateMonitor {

	private static Logger logger = LoggerFactory.getLogger(HitRateMonitor.class);

	private static final String hitRateRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"squirrel-client.monitor.hitrate.range", "1,5,10,20,30,40,50,60,70,80,90,100");

	private static int[] hitRateRangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"squirrel-client.monitor.hitrate.enable", true);

	private static class HitRateHolder {
		public static final HitRateMonitor INSTANCE = new HitRateMonitor();
	}

	public static HitRateMonitor getInstance() {
		return HitRateHolder.INSTANCE;
	}

	private HitRateMonitor() {
		if (enableMonitor) {
			init();
		}
	}

	private void init() {
		hitRateRangeArray = initRangeArray(hitRateRangeConfig);
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

	public void logHitRate(String cacheType, String category, String eventName, int hitRate, int hits) {
		if (enableMonitor && cacheType != null && !"web".equalsIgnoreCase(cacheType)) {
			try {
				log(hitRate, hitRateRangeArray, "Squirrel." + cacheType + ".multi.hitRate", hits);
			} catch (Throwable t) {
				logger.warn("error while logging key hit rate:" + t.getMessage());
			}
		}
	}

	private void log(int hitRate, int[] rangeArray, String eventName, int hits) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = "=" + rangeArray[rangeArray.length - 1];
			if (hitRate < rangeArray.length) {
				value = "<=" + rangeArray[hitRate];
			}
			Cat.getProducer().logEvent(eventName, value, Event.SUCCESS, hitRate + "%," + hits);
		}
	}
}
