package com.dianping.cache.autoscale;

public class Instance {
	
	private String appid;
	
	private String instanceid;
	
	private String ip;
	
	private String agentip;
	
	public Instance(){
		
	}
	
	public Instance(String instanceid,String ip){
		this.instanceid = instanceid;
		this.ip = ip;
	}
	
	public Instance(String instanceid, String ip,String agentip) {
		super();
		this.agentip = agentip;
		this.instanceid = instanceid;
		this.ip = ip;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(String instanceid) {
		this.instanceid = instanceid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAgentip() {
		return agentip;
	}

	public void setAgentip(String agentip) {
		this.agentip = agentip;
	}
	
}
