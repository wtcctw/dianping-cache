package com.dianping.cache.monitor.statsdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cache.scale.impl.RedisNode;

public class RedisClusterData {
	
	private String clusterName;
	
	private List<RedisNode> nodes;
	
	private long maxMemory;
	
	private long usedMemory;

	private float used;
	
	private String config; //private AlarmConfig config = null;
	
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
		// TODO Auto-generated method stub
		this.used = used;
	}
	
	public float getUsed(){
		return used;
	}
	
	public void check(){
		if(config != null){
			
		}else{ //default
			//way 1:  callback     config.check(this)
			//init
			flags.put("used", 1);
			colors.put("used", "green");
			flags.put("mem",1);
			colors.put("mem", "green");
			if(maxMemory == 0l){
				flags.put("mem", 4);
				colors.put("mem", "red");
			}
			
			
			if(used > 90){
				flags.put("used", 4);
				colors.put("used", "red");
			}else if(used > 50){
				flags.put("used", 2);
				colors.put("used", "orange");
			}
			
			Integer value = 1;
			for(Entry<String, Integer> entry : flags.entrySet()){
				value = entry.getValue() | value;
			}
			flags.put("alarm", value);
			if(value >= 4)
				colors.put("alarm", "red");
			else if(value >= 2)
				colors.put("alarm", "orange");
			else
				colors.put("alarm", "green");
		}
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
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
