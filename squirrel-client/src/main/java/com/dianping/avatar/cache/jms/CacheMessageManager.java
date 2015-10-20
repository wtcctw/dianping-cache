package com.dianping.avatar.cache.jms;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.avatar.cache.client.RemoteCacheClientFactory;
import com.dianping.avatar.cache.configuration.RemoteCacheItemConfigManager;
import com.dianping.cache.core.CacheConfigurationListener;
import com.dianping.cache.util.ZKUtils;
import com.dianping.remote.cache.dto.CacheConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyConfigurationDTO;
import com.dianping.remote.cache.dto.CacheKeyTypeVersionUpdateDTO;
import com.dianping.remote.cache.dto.SingleCacheRemoveDTO;

public class CacheMessageManager implements CacheConfigurationListener {

    private static Logger logger = LoggerFactory.getLogger(CacheMessageManager.class);
    
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
		return RemoteCacheItemConfigManager.getInstance().getCacheKeyType(versionChange.getMsgValue()) != null;
	}

	public static boolean isInterestedMessage(CacheConfigurationDTO serviceChange) {
		return RemoteCacheClientFactory.getInstance().getCacheClientConfig(serviceChange.getCacheKey()) != null;
	}

	public static boolean isInterestedMessage(CacheKeyConfigurationDTO categoryChange) {
		return RemoteCacheItemConfigManager.getInstance().getCacheKeyType(categoryChange.getCategory()) != null;
	}

	public static boolean isInterestedMessage(SingleCacheRemoveDTO keyRemove) {
		String category = ZKUtils.getCategoryFromKey(keyRemove.getCacheKey());
		return RemoteCacheItemConfigManager.getInstance().getCacheKeyType(category) != null;
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
