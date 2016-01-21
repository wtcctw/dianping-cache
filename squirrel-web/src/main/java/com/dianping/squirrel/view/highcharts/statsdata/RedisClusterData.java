package com.dianping.squirrel.view.highcharts.statsdata;

import com.dianping.cache.scale.cluster.redis.RedisNode;
import com.dianping.cache.scale.cluster.redis.RedisServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RedisClusterData {
	
	private String clusterName;
	
	private List<RedisNode> nodes;

	private List<RedisServer> failedServers;
	
	private long maxMemory;
	
	private long usedMemory;

	private int masterNum;

	private int slaveNum;

	private int qps;

	private float used;

	private boolean migrate;

	private String config;

	
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

	public void check(){
		ColorsCheck check = new ColorsCheck();
		check.check();
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

	public int getMasterNum() {
		return masterNum;
	}

	public void setMasterNum(int masterNum) {
		this.masterNum = masterNum;
	}

	public int getSlaveNum() {
		return slaveNum;
	}

	public void setSlaveNum(int slaveNum) {
		this.slaveNum = slaveNum;
	}

	public boolean isMigrate() {
		return migrate;
	}

	public void setMigrate(boolean migrate) {
		this.migrate = migrate;
	}

	public List<RedisServer> getFailedServers() {
		return failedServers;
	}

	public void setFailedServers(List<RedisServer> failedServers) {
		this.failedServers = failedServers;
	}

	public int getQps() {
		return qps;
	}

	public void setQps(int qps) {
		this.qps = qps;
	}

	class ColorsCheck{

		private int alarm = 1;
		private final int DANGER = 4;

		private float memUsedDanger = 80.0f;
		private int qpsDanger = 90000;
		public  void check(){
			colors.put("used",switchColors(switchColors(used,memUsedDanger)));
			if(maxMemory == 0L){
				alarm = 4;
				colors.put("used","red");
			}
			colors.put("dead",switchColors(switchColors(masterNum,slaveNum+1)));
			colors.put("qps",switchColors(switchColors(qps,qpsDanger)));
			colors.put("alarm",switchColors(alarm,DANGER));
		}

		private String switchColors(int value,int danger){
			if(value >= danger){
				alarm = DANGER;
				return "red";
			}
			return "green";
		}

		private String switchColors(float value,float danger){
			if(value >= danger){
				alarm = DANGER;
				return "red";
			}
			return "green";
		}

		private String switchColors(long value,long danger){
			if(value >= danger){
				alarm = DANGER;
				return "red";
			}
			return "green";
		}

		private String switchColors(String color){
			return "green".equals(color) ? "" : color;
		}
	}
}
