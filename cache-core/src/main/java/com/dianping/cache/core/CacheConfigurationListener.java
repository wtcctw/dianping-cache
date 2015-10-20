package com.dianping.cache.core;

public interface CacheConfigurationListener {

	public boolean isVersionChanged(String category, int recentSeconds);

	public boolean isCategoryChanged(String category, int recentSeconds);
}
