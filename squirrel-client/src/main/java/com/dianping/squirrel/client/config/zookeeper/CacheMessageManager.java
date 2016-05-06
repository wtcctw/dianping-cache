package com.dianping.squirrel.client.config.zookeeper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.StoreClientConfigManager;
import com.dianping.squirrel.client.core.CacheConfigurationListener;
import com.dianping.squirrel.common.domain.CacheConfigurationDTO;
import com.dianping.squirrel.common.domain.CacheKeyConfigurationDTO;
import com.dianping.squirrel.common.domain.CacheKeyTypeVersionUpdateDTO;
import com.dianping.squirrel.common.domain.SingleCacheRemoveDTO;
import com.dianping.squirrel.common.util.PathUtils;

public class CacheMessageManager implements CacheConfigurationListener {

	private static final CacheMessageManager INSTANCE = new CacheMessageManager();

	private static ConcurrentMap<String, Long> versionChangeMap = new ConcurrentHashMap<String, Long>();
	private static ConcurrentMap<String, Long> serviceChangeMap = new ConcurrentHashMap<String, Long>();
	private static ConcurrentMap<String, Long> categoryChangeMap = new ConcurrentHashMap<String, Long>();

	private static ConcurrentMap<String, Long> versionChangeTimeMap = new ConcurrentHashMap<String, Long>();

	private CacheMessageManager() {
	}

	public static CacheMessageManager getInstance() {
		return INSTANCE;
	}

	public static boolean takeMessage(CacheKeyTypeVersionUpdateDTO versionChange) {
		Long lastest = versionChangeMap.putIfAbsent(versionChange.getMsgValue(), versionChange.getAddTime());
		if (lastest == null) {
			versionChangeTimeMap.put(versionChange.getMsgValue(), versionChange.getAddTime());
			return true;
		} else {
			while (versionChange.getAddTime() > lastest) {
				if (versionChangeMap.replace(versionChange.getMsgValue(), lastest, versionChange.getAddTime())) {
					versionChangeTimeMap.put(versionChange.getMsgValue(), System.currentTimeMillis());
					return true;
				} else {
					lastest = versionChangeMap.get(versionChange.getMsgValue());
				}
			}
			return false;
		}
	}

	public static boolean takeMessage(CacheConfigurationDTO serviceChange) {
		Long lastest = serviceChangeMap.putIfAbsent(serviceChange.getCacheKey(), serviceChange.getAddTime());
		if (lastest == null) {
			return true;
		} else {
			while (serviceChange.getAddTime() > lastest) {
				if (serviceChangeMap.replace(serviceChange.getCacheKey(), lastest, serviceChange.getAddTime())) {
					return true;
				} else {
					lastest = serviceChangeMap.get(serviceChange.getCacheKey());
				}
			}
			return false;
		}
	}

	public static boolean takeMessage(CacheKeyConfigurationDTO categoryChange) {
		Long lastest = categoryChangeMap.putIfAbsent(categoryChange.getCategory(), categoryChange.getAddTime());
		if (lastest == null) {
			return true;
		} else {
			while (categoryChange.getAddTime() > lastest) {
				if (categoryChangeMap.replace(categoryChange.getCategory(), lastest, categoryChange.getAddTime())) {
					return true;
				} else {
					lastest = categoryChangeMap.get(categoryChange.getCategory());
				}
			}
			return false;
		}
	}

	public static boolean isInterestedMessage(CacheKeyTypeVersionUpdateDTO versionChange) {
		return StoreCategoryConfigManager.getInstance().getCacheKeyType(versionChange.getMsgValue()) != null;
	}

	public static boolean isInterestedMessage(CacheConfigurationDTO serviceChange) {
		return StoreClientConfigManager.getInstance().getCacheClientConfig(serviceChange.getCacheKey()) != null;
	}

	public static boolean isInterestedMessage(CacheKeyConfigurationDTO categoryChange) {
		return StoreCategoryConfigManager.getInstance().getCacheKeyType(categoryChange.getCategory()) != null;
	}

	public static boolean isInterestedMessage(SingleCacheRemoveDTO keyRemove) {
		String category = PathUtils.getCategoryFromKey(keyRemove.getCacheKey());
		return StoreCategoryConfigManager.getInstance().getCacheKeyType(category) != null;
	}

	public boolean isCategoryChanged(String category, int recentSeconds) {
		Long changeTime = categoryChangeMap.get(category);
		if (changeTime == null) {
			return false;
		} else {
			return (System.currentTimeMillis() - changeTime) < recentSeconds * 1000;
		}
	}

	public boolean isVersionChanged(String category, int recentSeconds) {
		Long changeTime = versionChangeTimeMap.get(category);
		if (changeTime == null) {
			return false;
		} else {
			long cost = (System.currentTimeMillis() - changeTime);
			return cost < recentSeconds * 1000;
		}
	}
}
