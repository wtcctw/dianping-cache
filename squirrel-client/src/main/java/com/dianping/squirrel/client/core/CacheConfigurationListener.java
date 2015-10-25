package com.dianping.squirrel.client.core;

public interface CacheConfigurationListener {

	public boolean isVersionChanged(String category, int recentSeconds);

	public boolean isCategoryChanged(String category, int recentSeconds);
}
