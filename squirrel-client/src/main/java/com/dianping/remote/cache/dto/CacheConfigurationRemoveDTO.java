package com.dianping.remote.cache.dto;

public class CacheConfigurationRemoveDTO extends AbstractDTO{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3061572290802782351L;
	
    private String cacheKey;
    
    private String clientClazz;

    private String servers;

    private String transcoderClazz;
    
    private long addTime = System.currentTimeMillis();

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getClientClazz() {
		return clientClazz;
	}

	public void setClientClazz(String clientClazz) {
		this.clientClazz = clientClazz;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public String getTranscoderClazz() {
		return transcoderClazz;
	}

	public void setTranscoderClazz(String transcoderClazz) {
		this.transcoderClazz = transcoderClazz;
	}

	@Override
	public String toString() {
		return "Cache : " + this.cacheKey + "\n clientClazz : " + this.clientClazz + " \n servers : " + this.servers;
	}
    
	
}
