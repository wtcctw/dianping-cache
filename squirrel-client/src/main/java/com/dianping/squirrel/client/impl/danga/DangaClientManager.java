package com.dianping.squirrel.client.impl.danga;

import com.danga.MemCached.MemCachedClient;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DangaClientManager {
	private DangaClientConfig config;
	
	private MemCachedClient readClient;

	private MemCachedClient writeClient;

	private ThreadPoolExecutor threadPool;
	
	public DangaClientManager(DangaClientConfig config){
		this.config = config;
	}
	
	public MemCachedClient getReadClient() {
		if(readClient != null)
			return readClient;
		return null;
	}
	
	public MemCachedClient getWriteClient() {
		if(writeClient != null)
			return writeClient;
		return null;
	}
	
	public void start(){
		String servers = config.getServers();
		if (servers == null || servers.length() < 1) {
			throw new RuntimeException("Server address must be specified.");
		}
		if (!servers.contains(",")) {
			// memcached
			String[] serverSplits = config.getServers().split(" ");
			readClient = DangaClientFactory.createClient(serverSplits, config.getReadPoolName(),config.getTranscoder());
			writeClient = DangaClientFactory.createClient(serverSplits, config.getWritePoolName(),config.getTranscoder());
		} else {
			// kvdb
			String[] serverSplits = servers.split(" ");
			String writeServer = serverSplits[0].trim();
			String readServers = serverSplits.length == 1 ? writeServer : serverSplits[1].trim();
			readClient = DangaClientFactory.createClient(new String[]{readServers}, config.getReadPoolName(),config.getTranscoder());
			writeClient = DangaClientFactory.createClient(new String[]{writeServer}, config.getWritePoolName(),config.getTranscoder());
		}
	}
	
	public void stop(){
		readClient = null;
		writeClient = null;
	}

	public ThreadPoolExecutor getThreadPool() {
		if(threadPool == null)
			synchronized (this){
				if(threadPool == null){
					threadPool = new ThreadPoolExecutor(DangaClientFactory.getCoreSize(),DangaClientFactory.getMaxSize(),
							1000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
				}
			}
		return threadPool;
	}
}
