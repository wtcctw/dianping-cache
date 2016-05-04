package com.dianping.squirrel.client.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class QpsMonitor {

	private static Logger logger = LoggerFactory.getLogger(QpsMonitor.class);

	private static final String rangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"squirrel-client.monitor.qps.range",
			"10,50,100,300,500,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,12000,14000,16000,18000,20000,25000,30000");

	private static int[] rangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager()
			.getBooleanValue("squirrel-client.monitor.qps.enable", false);

	private static class KeyCountHolder {
		public static final QpsMonitor INSTANCE = new QpsMonitor();
	}

	public static QpsMonitor getInstance() {
		return KeyCountHolder.INSTANCE;
	}

	private QpsMonitor() {
		if (enableMonitor) {
			init();
		}
	}

	private void init() {
		rangeArray = initRangeArray(rangeConfig);
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

	public void logQps(String eventName, int count) {
		if (enableMonitor && !eventName.startsWith("Cache.web.")) {
			try {
				log(count, rangeArray, eventName);
			} catch (Throwable t) {
				logger.warn("error while logging qps:" + t.getMessage());
			}
		}
	}

	private void log(int size, int[] rangeArray, String eventName) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1];
			if (rangeArray.length > size) {
				value = "<" + rangeArray[size];
			}
			Cat.getProducer().logEvent(eventName, value, Event.SUCCESS, size + "");
		}
	}
}
