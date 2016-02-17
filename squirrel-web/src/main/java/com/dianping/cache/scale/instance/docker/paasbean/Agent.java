package com.dianping.cache.scale.instance.docker.paasbean;

import java.util.ArrayList;
import java.util.List;

public class Agent {
	private long id;

	private String ip;

	private Integer cpu;

	private Integer version;

	private Integer status;

	private Zone zone;

	private Memory memory;

	private Disk disk;

	private List<Instance> instances = new ArrayList<Instance>();

	private IpPool ipPool;

	private List<Group> groups = new ArrayList<Group>();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getCpu() {
		return cpu;
	}

	public void setCpu(Integer cpu) {
		this.cpu = cpu;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public Disk getDisk() {
		return disk;
	}

	public void setDisk(Disk disk) {
		this.disk = disk;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public IpPool getIpPool() {
		return ipPool;
	}

	public void setIpPool(IpPool ipPool) {
		this.ipPool = ipPool;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

}
