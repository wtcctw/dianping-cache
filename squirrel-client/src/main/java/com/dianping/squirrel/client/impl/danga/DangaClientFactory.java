package com.dianping.squirrel.client.impl.danga;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.schooner.MemCached.TransCoder;

/**
 * Created by dp on 15/11/30.
 */
public class DangaClientFactory {

	private static final ConfigManager configManager = ConfigManagerLoader
			.getConfigManager();

	private static final String INIT_CONNECTION_NUMBER = "avatar-cache.danga.initconn";
	private static final int DEFAULT_INIT_CONNECTION_NUMBER = 10;

	private static final String MIN_CONNECTION_NUMBER = "avatar-cache.danga.minconn";
	private static final int DEFAULT_MIN_CONNECTION_NUMBER = 5;

	private static final String MAX_CONNECTION_NUMBER = "avatar-cache.danga.maxconn";
	private static final int DEFAULT_MAX_CONNECTION_NUMBER = 250;

	private static final String FAILOVER = "avatar-cache.danga.faliover";
	private static final boolean DEFAULT_FAILOVER = true;

	private static final String MAINTSLEEP = "avatar-cache.danga.maintsleep";
	private static final int DEFAULT_MAINTSLEEP = 30;

	private static final String ISNAGLE = "avatar-cache.danga.nagle";
	private static final boolean DEFAULT_ISNAGLE = false;

	private static final String SOCKET_READTIMEOUT = "avatar-cache.danga.socket_readtimeout";
	private static final int DEFAULT_SOCKET_READTIMEOUT = 3000;

	private static final String ALIVECHECK = "avatar-cache.danga.alivecheck";
	private static final boolean DEFAULT_ALIVECHECK = true;

	private static int initConn = configManager.getIntValue(
			INIT_CONNECTION_NUMBER, DEFAULT_INIT_CONNECTION_NUMBER);
	private static int minConn = configManager.getIntValue(
			MIN_CONNECTION_NUMBER, DEFAULT_MIN_CONNECTION_NUMBER);
	private static int maxConn = configManager.getIntValue(
			MAX_CONNECTION_NUMBER, DEFAULT_MAX_CONNECTION_NUMBER);
	private static boolean failover = configManager.getBooleanValue(FAILOVER,
			DEFAULT_FAILOVER);
	private static int maintsleep = configManager.getIntValue(MAINTSLEEP,
			DEFAULT_MAINTSLEEP);
	private static boolean nagle = configManager.getBooleanValue(ISNAGLE,
			DEFAULT_ISNAGLE);
	private static int socketTO = configManager.getIntValue(SOCKET_READTIMEOUT,
			DEFAULT_SOCKET_READTIMEOUT);
	private static boolean alivecheck = configManager.getBooleanValue(
			ALIVECHECK, DEFAULT_ALIVECHECK);

	public static MemCachedClient createClient(String[] servers, String poolName,TransCoder transCoder) {

		MemCachedClient client = null;
		SockIOPool pool = SockIOPool.getInstance(poolName);
		pool.setServers(servers);// 设置连接池可用的cache服务器列表
		pool.setFailover(failover);// 设置容错开关
		pool.setInitConn(initConn);// 设置开始时每个cache服务器的可用连接数
		pool.setMinConn(minConn);// 设置每个服务器最少可用连接数
		pool.setMaxConn(maxConn);// 设置每个服务器最大可用连接数
		pool.setMaintSleep(maintsleep);// 设置连接池维护线程的睡眠时间
		pool.setNagle(nagle);// 设置是否使用Nagle算法，因为我们的通讯数据量通常都比较大（相对TCP控制数据）而且要求响应及时，因此该值需要设置为false（默认是true）
		pool.setSocketTO(socketTO);// 设置socket的读取等待超时值
		pool.setAliveCheck(alivecheck);// 设置连接心跳监测开关
		pool.initialize();
		client = new MemCachedClient(poolName,true,false);
		client.setTransCoder(transCoder);
		client.setPrimitiveAsString(true);
		return client;
	}

}
