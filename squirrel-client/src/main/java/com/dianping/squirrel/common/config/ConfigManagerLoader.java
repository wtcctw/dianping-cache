package com.dianping.squirrel.common.config;

public class ConfigManagerLoader {

	public static final ConfigManager configManager = new LionConfigManager();

	public static ConfigManager getConfigManager() {
		return configManager;
	}
}
