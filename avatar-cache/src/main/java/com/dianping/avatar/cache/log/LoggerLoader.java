/**
 * 
 */
package com.dianping.avatar.cache.log;

public class LoggerLoader {

	private LoggerLoader() {
	}

	private static volatile boolean customLog4j = false;

	private static volatile boolean inited = false;

	static {
		try {
			Class.forName("org.apache.log4j.Hierarchy");
			customLog4j = true;
		} catch (ClassNotFoundException e) {
			customLog4j = false;
		}
	}

	public static void init() {
		if (!inited && customLog4j) {
			CustomLog4jFactory.init();
			inited = true;
		}
	}

}
