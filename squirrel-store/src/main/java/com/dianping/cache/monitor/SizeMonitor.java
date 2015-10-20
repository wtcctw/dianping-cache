package com.dianping.cache.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManagerLoader;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;

public class SizeMonitor {

	private static Logger logger = LoggerFactory.getLogger(SizeMonitor.class);

	private static final String requestSizeRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"avatar-cache.monitor.size.requestrange", "1,2,4,8,16,32,64,128,256,512,1024");

	private static final String responseSizeRangeConfig = ConfigManagerLoader.getConfigManager().getStringValue(
			"avatar-cache.monitor.size.responserange", "1,2,4,8,16,32,64,128,256,512,1024");

	private static int[] requestSizeRangeArray;

	private static int[] responseSizeRangeArray;

	private static final boolean enableMonitor = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"avatar-cache.monitor.size.enable", true);

	private static final long sizeMin = ConfigManagerLoader.getConfigManager().getLongValue(
			"avatar-cache.monitor.size.min", 65536);

	private static class CacheSizeHolder {
		public static final SizeMonitor INSTANCE = new SizeMonitor();
	}

	public static SizeMonitor getInstance() {
		return CacheSizeHolder.INSTANCE;
	}

	private SizeMonitor() {
		if (enableMonitor) {
			init();
		}
	}

	private void init() {
		requestSizeRangeArray = initRangeArray(requestSizeRangeConfig);
		responseSizeRangeArray = initRangeArray(responseSizeRangeConfig);
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

	public void logRequestSize(String eventName, int size) {
		if (enableMonitor && size > sizeMin) {
			try {
				logSize(size, requestSizeRangeArray, eventName);
			} catch (Throwable t) {
				logger.warn("error while logging request size:" + t.getMessage());
			}
		}
	}

	public void logResponseSize(String eventName, int size) {
		if (enableMonitor && size > sizeMin) {
			try {
				logSize(size, responseSizeRangeArray, eventName);
			} catch (Throwable t) {
				logger.warn("error while logging response size:" + t.getMessage());
			}
		}
	}

	private void logSize(int size, int[] rangeArray, String eventName) {
		if (rangeArray != null && rangeArray.length > 0) {
			String value = ">" + rangeArray[rangeArray.length - 1] + "k";
			int sizeK = (int) Math.ceil(size * 1d / 1024);
			if (rangeArray.length > sizeK) {
				value = "<" + rangeArray[sizeK] + "k";
			}
			Cat.getProducer().logEvent(eventName, value, Event.SUCCESS, size + "");
		}
	}
}
