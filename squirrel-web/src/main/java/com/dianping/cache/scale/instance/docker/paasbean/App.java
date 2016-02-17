package com.dianping.cache.scale.instance.docker.paasbean;

public class App {
	private String appId;

	private String appVersion;

	private Integer appLevel;

	private Double cpuNum;

	private Integer cpuMode;

	private long memorySize;

	private long diskSize;

	private String idc;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public Integer getAppLevel() {
		return appLevel;
	}

	public void setAppLevel(Integer appLevel) {
		this.appLevel = appLevel;
	}

	public Double getCpuNum() {
		return cpuNum;
	}

	public void setCpuNum(Double cpuNum) {
		this.cpuNum = cpuNum;
	}

	public Integer getCpuMode() {
		return cpuMode;
	}

	public void setCpuMode(Integer cpuMode) {
		this.cpuMode = cpuMode;
	}

	public long getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(long memorySize) {
		this.memorySize = memorySize;
	}

	public long getDiskSize() {
		return diskSize;
	}

	public void setDiskSize(long diskSize) {
		this.diskSize = diskSize;
	}

	public String getIdc() {
		return idc;
	}

	public void setIdc(String idc) {
		this.idc = idc;
	}

}
