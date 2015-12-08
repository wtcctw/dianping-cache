package com.dianping.squirrel.client.impl.danga;

import java.util.ArrayList;
import java.util.List;

import com.dianping.squirrel.client.config.StoreClientConfig;
import com.dianping.squirrel.client.core.CacheConfigurationListener;
import com.dianping.squirrel.common.exception.StoreInitializeException;
import com.schooner.MemCached.TransCoder;

public class DangaClientConfig  implements StoreClientConfig {
	/**
	 * All servers
	 */
	private List<String> servers = new ArrayList<String>();
	/**
	 * Transcoder
	 */
	private TransCoder transcoder;
	
	private String poolName;

	private String clientClazz = "com.dianping.squirrel.client.impl.danga.DangaStoreClientImpl";

	private CacheConfigurationListener cacheConfigurationListener;

	public CacheConfigurationListener getCacheConfigurationListener() {
		return cacheConfigurationListener;
	}

	public void setCacheConfigurationListener(CacheConfigurationListener cacheConfigurationListener) {
		this.cacheConfigurationListener = cacheConfigurationListener;
	}

	/**
	 * Add memcached server and prot
	 */
	public void addServer(String server, int port) {
		addServer(server + ":" + port);
	}

	public void addServer(String address) {
		servers.add(address);
	}

	public void setServerList(List<String> servers) {
		this.servers = servers;
	}

	public List<String> getServerList() {
		return this.servers;
	}

	public String getServers() {

		StringBuffer sb = new StringBuffer();

		for (String server : servers) {
			sb.append(server.trim());
			sb.append(" ");
		}

		return sb.toString().trim();
	}

	@Override
	public void init() throws StoreInitializeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClientClazz(String clientClazz) {
		this.clientClazz = clientClazz;
		
	}

	@Override
	public String getClientClazz() {
		return this.clientClazz;
	}

	/**
	 * @return the transcoder
	 */
	public TransCoder getTranscoder() {
		return transcoder;
	}

	/**
	 * @param transcoder
	 *            the transcoder to set
	 */
	public void setTranscoder(TransCoder transcoder) {
		this.transcoder = transcoder;
	}
	
	public void setTranscoderClass(Class cz) throws Exception{
		TransCoder t = (TransCoder)cz.newInstance();
		setTranscoder(t);
	}
	
	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	
	public String getReadPoolName(){
		return poolName + "read";
	}
	
	public String getWritePoolName(){
		return poolName + "write";
	}
}
