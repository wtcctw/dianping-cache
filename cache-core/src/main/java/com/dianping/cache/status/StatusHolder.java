package com.dianping.cache.status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManagerLoader;

public final class StatusHolder {

	private static final Logger logger = LoggerFactory.getLogger(StatusHolder.class);

	private static ConcurrentHashMap<String, CapacityBucket> capacityBuckets = new ConcurrentHashMap<String, CapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"avatar-cache.stat.enable", true);

	public static void init() {
		Thread t = new Thread(new StatusChecker());
		t.setDaemon(true);
		t.start();
	}

	public static Map<String, CapacityBucket> getCapacityBuckets() {
		return capacityBuckets;
	}

	public static CapacityBucket getCapacityBucket(String source) {
		CapacityBucket barrel = capacityBuckets.get(source);
		if (barrel == null) {
			CapacityBucket newBarrel = new CapacityBucket(source);
			barrel = capacityBuckets.putIfAbsent(source, newBarrel);
			if (barrel == null) {
				barrel = newBarrel;
			}
		}
		return barrel;
	}

	public static void flowIn(String cacheType, String category, String source) {
		if (checkRequestNeedStat(cacheType, category, source)) {
			String key = cacheType + "." + source;
			CapacityBucket barrel = getCapacityBucket(key);
			if (barrel != null) {
				barrel.flowIn();
			}
		}
	}

	public static void flowOut(String cacheType, String category, String source) {
		if (checkRequestNeedStat(cacheType, category, source)) {
			String key = cacheType + "." + source;
			CapacityBucket barrel = getCapacityBucket(key);
			if (barrel != null) {
				barrel.flowOut();
			}
		}
	}

	public static boolean checkRequestNeedStat(String cacheType, String category, String source) {
		return statEnable && cacheType != null && !"web".equalsIgnoreCase(cacheType);
	}

	public static void removeCapacityBucket(String source) {
		capacityBuckets.remove(source);
	}
}
