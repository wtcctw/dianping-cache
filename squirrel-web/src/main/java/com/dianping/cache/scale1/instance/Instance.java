package com.dianping.cache.scale1.instance;

public class Instance {
	
	private AppId appId;
	
	private String instanceId;
	
	private String ip;
	
	private String agentIp;
	
	public Instance(){
	}
	
	public Instance(String instanceId,String ip){
		this.instanceId = instanceId;
		this.ip = ip;
	}
	
	public Instance(String instanceid, String ip,String agentip) {
		super();
		this.agentIp = agentip;
		this.instanceId = instanceid;
		this.ip = ip;
	}
	
	public Instance(AppId appId,String instanceid, String ip,String agentip) {
		super();
		this.appId = appId;
		this.agentIp = agentip;
		this.instanceId = instanceid;
		this.ip = ip;
	}
	
	public String getAddress(){
		return ip + ":" + appId.getPort();
	}
	
	public AppId getAppId() {
		return appId;
	}

	public void setAppId(AppId appId) {
		this.appId = appId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAgentIp() {
		return agentIp;
	}

	public void setAgentIp(String agentIp) {
		this.agentIp = agentIp;
	}

}