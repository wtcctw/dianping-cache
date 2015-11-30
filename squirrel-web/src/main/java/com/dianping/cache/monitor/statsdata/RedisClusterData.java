package com.dianping.cache.monitor.statsdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cache.monitor.monitorcheck.RedisMonitorCheck;
import com.dianping.cache.scale.impl.RedisNode;

public class RedisClusterData {
	
	private String clusterName;
	
	private List<RedisNode> nodes;
	
	private long maxMemory;
	
	private long usedMemory;

	private float used;
	
	private Map<String,Integer> flags = new HashMap<String,Integer>();
	
	private Map<String,String> colors = new HashMap<String,String>();

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
		this.used = used;
	}
	
	public float getUsed(){
		return used;
	}
	
	public void check() {
		RedisMonitorCheck check = new RedisMonitorCheck();
		check.check(this);
	}

	public Map<String, Integer> getFlags() {
		return flags;
	}

	public void setFlags(Map<String, Integer> flags) {
		this.flags = flags;
	}

	public Map<String, String> getColors() {
		return colors;
	}

	public void setColors(Map<String, String> colors) {
		this.colors = colors;
	}
	
}
