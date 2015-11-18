package com.dianping.cache.scale.impl;

public class RedisInfo {
	
	private long usedMemory;
	
	private long maxMemory;

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(long maxMemory) {
		this.maxMemory = maxMemory;
	}
	
	
}
