package com.dianping.cache.scale.instance.docker.paasbean;


public class Container {
	private String agentIp;

	private String instanceId;

	private String instanceIp;

	private Integer port;

	private String appId;

	private String cpuSet;

	private Integer cpuShare;

	private Long memoryLimit;

	private String hostname;

	private String status;

	private String startedAt;

	public String getAgentIp() {
		return agentIp;
	}

	public void setAgentIp(String agentIp) {
		this.agentIp = agentIp;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceIp() {
		return instanceIp;
	}

	public void setInstanceIp(String instanceIp) {
		this.instanceIp = instanceIp;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getCpuSet() {
		return cpuSet;
	}

	public void setCpuSet(String cpuSet) {
		this.cpuSet = cpuSet;
	}

	public Integer getCpuShare() {
		return cpuShare;
	}

	public void setCpuShare(Integer cpuShare) {
		this.cpuShare = cpuShare;
	}

	public Long getMemoryLimit() {
		return memoryLimit;
	}

	public void setMemoryLimit(Long memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(String startedAt) {
		this.startedAt = startedAt;
	}
}
