package com.dianping.cache.scale.instance.docker.paasbean;


public class MachineBean {
   private long id;

   private String agent;

   private int version;

   private int cpu;

   private String ip;

   private long memory;

   private long disk;

   private String idc;

   private String switcher;

   private String frame;

   private long networkId;

   private int status;

   private String hostname;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getMemory() {
		return memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}

	public long getDisk() {
		return disk;
	}

	public void setDisk(long disk) {
		this.disk = disk;
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

	public long getNetworkId() {
		return networkId;
	}

	public void setNetworkId(long networkId) {
		this.networkId = networkId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getHostname() {
	   return hostname;
   }

	public void setHostname(String hostname) {
	   this.hostname = hostname;
   }

}
