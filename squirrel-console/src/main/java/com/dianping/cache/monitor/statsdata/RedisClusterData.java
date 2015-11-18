package com.dianping.cache.monitor.statsdata;

import java.util.List;

import com.dianping.cache.scale.impl.RedisNode;

public class RedisClusterData {
	
	private String clusterName;
	
	private List<RedisNode> nodes;
	
	private long maxMemory;
	
	private long usedMemory;

	private float used;

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(long maxMemory) {
		this.maxMemory = maxMemory;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public List<RedisNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RedisNode> nodes) {
		this.nodes = nodes;
	}

	public void setUsed(float used) {
		// TODO Auto-generated method stub
		this.used = used;
	}
	
	public float getUsed(){
		return used;
	}
	
}
