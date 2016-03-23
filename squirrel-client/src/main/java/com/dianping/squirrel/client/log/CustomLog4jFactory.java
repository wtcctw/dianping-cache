package com.dianping.squirrel.client.log;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;

public class CustomLog4jFactory {

	public static void init() {
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
