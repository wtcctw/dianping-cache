package com.dianping.squirrel.client.log;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class CustomLog4jFactory {
	private static final String LOG_DIR_PROPERTY_KEY = "squirrel.log.dir";
	private static final String DEFAULT_LOG_DIR = "/data/applogs/squirrel/squirrel.log";

	public static void init() {
		//System.getProperties().putIfAbsent(LOG_DIR_PROPERTY_KEY,DEFAULT_LOG_DIR);
		if(System.getProperty(LOG_DIR_PROPERTY_KEY) == null){
			System.setProperty(LOG_DIR_PROPERTY_KEY,DEFAULT_LOG_DIR);
		}
		new DOMConfigurator().doConfigure(CustomLog4jFactory.class.getClassLoader().getResource("squirrel_log4j.xml"),
				LogManager.getLoggerRepository());
		ConfigManager configManager = ConfigManagerLoader.getConfigManager();
		boolean logConsole = configManager.getBooleanValue("squirrel.log.console", true);
		if(!logConsole) {
		    Logger.getLogger("com.dianping.squirrel").removeAppender("SquirrelConsoleAppender");
		    Logger.getLogger("redis.clients.jedis").removeAppender("SquirrelConsoleAppender");
		}
	}
	
}
