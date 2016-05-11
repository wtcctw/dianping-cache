package com.dianping.cache.scale.instance.docker.paasbean;

import java.util.ArrayList;
import java.util.List;

public class MachineStatusBean {
	private long id;

	private int version;

	private int cpu;

	private List<Integer> coresFree = new ArrayList<Integer>();

	private String ip;

	private long memory;

	private long disk;

    private long basesize;

    private String idc;

	private String switcher;

	private String frame;

	private long networkId;

	private int status;

	private long keyId;

	private long memoryFree;

	private String zone;

	private long diskFree;

	private int instanceNum;

   private List<Container> containers = new ArrayList<Container>();

   private List<Group> cpuGroups = new ArrayList<Group>();

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

	public List<Integer> getCoresFree() {
		return coresFree;
	}

	public void setCoresFree(List<Integer> coresFree) {
		this.coresFree = coresFree;
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

	public long getKeyId() {
		return keyId;
	}

	public void setKeyId(long keyId) {
		this.keyId = keyId;
	}

	public long getMemoryFree() {
		return memoryFree;
	}

	public void setMemoryFree(long memoryFree) {
		this.memoryFree = memoryFree;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public long getDiskFree() {
		return diskFree;
	}

	public void setDiskFree(long diskFree) {
		this.diskFree = diskFree;
	}

	public int getInstanceNum() {
		return instanceNum;
	}

	public void setInstanceNum(int instanceNum) {
		this.instanceNum = instanceNum;
	}

	public List<Container> getContainers() {
		return containers;
	}

	public void setContainers(List<Container> containerInfos) {
		this.containers = containerInfos;
	}

	public List<Group> getCpuGroups() {
		return cpuGroups;
	}

	public void setCpuGroups(List<Group> cpuGroups) {
		this.cpuGroups = cpuGroups;
	}

    public long getBasesize() {
        return basesize;
    }

    public void setBasesize(long basesize) {
        this.basesize = basesize;
    }
}
