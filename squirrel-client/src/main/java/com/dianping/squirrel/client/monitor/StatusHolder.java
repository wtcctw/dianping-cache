package com.dianping.squirrel.client.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.squirrel.common.config.ConfigManagerLoader;

public final class StatusHolder {

	private static ConcurrentHashMap<String, CapacityBucket> capacityBuckets = new ConcurrentHashMap<String, CapacityBucket>();

	public static final boolean statEnable = ConfigManagerLoader.getConfigManager()
			.getBooleanValue("squirrel-client.stat.enable", false);

	private static volatile boolean inited = false;

	public static synchronized void init() {
		if (!inited) {
			inited = true;
			Thread t = new Thread(new StatusChecker());
			t.setName("Squirrel-Status-Checker");
			t.setDaemon(true);
			t.start();
		}
	}

	public static Map<String, CapacityBucket> getCapacityBuckets() {
		return capacityBuckets;
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

	private static CapacityBucket getCapacityBucket(String source) {
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

	private static boolean checkRequestNeedStat(String cacheType, String category, String source) {
		return statEnable && cacheType != null && !"web".equalsIgnoreCase(cacheType);
	}
}
