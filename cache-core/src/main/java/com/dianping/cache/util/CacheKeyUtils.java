package com.dianping.cache.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class CacheKeyUtils {

	public static final String SUFFIX_LOCK = "_lock";
	public static final String SUFFIX_HOT = "_h";
	private static Random random = new Random();

	public static String getLastVersionCacheKey(String currentVersionCacheKey) {
		if (currentVersionCacheKey == null) {
			return currentVersionCacheKey;
		}
		int versionSplitPos = currentVersionCacheKey.lastIndexOf("_");
		if (versionSplitPos < 0) {
			return currentVersionCacheKey;
		}
		String versionStr = currentVersionCacheKey.substring(versionSplitPos + 1);
		if (!isNumeric(versionStr)) {
			return currentVersionCacheKey;
		}
		Integer currentVersion = Integer.valueOf(versionStr);
		if (currentVersion > 0) {
			return currentVersionCacheKey.substring(0, versionSplitPos + 1) + (currentVersion - 1);
		} else {
			return currentVersionCacheKey;
		}
	}

	private static boolean isNumeric(String src) {
		if (src == null || src.length() == 0) {
			return false;
		}
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) < '0' || src.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}

	public static Collection<String> reformKeys(Collection<String> keys) {
//		if (keys == null || keys.isEmpty()) {
//			return Collections.emptySet();
//		}
//		Collection<String> reformedKeys = new HashSet<String>(keys.size());
//		for (String key : keys) {
//			if (key.indexOf(' ') != -1) {
//				key = key.replace(" ", "@+~");
//			}
//			reformedKeys.add(key);
//		}
//		return reformedKeys;
	    return keys;
	}

	public static <T> Map<String, T> reformBackKeys(Map<String, T> keys) {
//		Map<String, T> reformedBacks = new HashMap<String, T>(keys.size());
//		for (Map.Entry<String, T> entry : keys.entrySet()) {
//			String key = entry.getKey();
//			if (key.indexOf("@+~") != -1) {
//				key = key.replace("@+~", " ");
//			}
//			reformedBacks.put(key, entry.getValue());
//		}
//		return reformedBacks;
	    return keys;
	}

	public static String getLockKey(String key) {
		String finalKey = (key != null ? key.replace(" ", "@+~") : key);
		return finalKey + SUFFIX_LOCK;
	}

	public static String reformKey(String key) {
		return reformKey(key, false);
	}

	public static String reformKey(String key, boolean isHot) {
		String finalKey = (key != null ? key.replace(" ", "@+~") : key);
		if (isHot) {
			return finalKey + SUFFIX_HOT;
		}
		return finalKey;
	}

	public static String nextCacheKey(String key, boolean isHot, int range) {
		if (isHot) {
			boolean isHit = random.nextInt(range) < 1;
			if (isHit) {
				return reformKey(key, true);
			}
		}
		return reformKey(key, false);
	}

}
