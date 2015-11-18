package com.dianping.cache.autoscale.dockerscale;

public class DockerInfo {
	private int id;
	private long memory;
	private String zone;
	private String ip;
	private String idc;
	private String switcher;
	private String frame;
	private int instanceNum;
	private long memoryFree;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getMemory() {
		return memory;
	}
	public void setMemory(long memory) {
		this.memory = memory;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getIdc() {
		return idc;
	}
	public void setIdc(String idc) {
		this.idc = idc;
	}
	public String getSwitcher() {
		return switcher;
	}
	public void setSwitcher(String switcher) {
		this.switcher = switcher;
	}
	public String getFrame() {
		return frame;
	}
	public void setFrame(String frame) {
		this.frame = frame;
	}
	public int getInstanceNum() {
		return instanceNum;
	}
	public void setInstanceNum(int instanceNum) {
		this.instanceNum = instanceNum;
	}
	public long getMemoryFree() {
		return memoryFree;
	}
	public void setMemoryFree(long memoryFree) {
		this.memoryFree = memoryFree;
	}
}
