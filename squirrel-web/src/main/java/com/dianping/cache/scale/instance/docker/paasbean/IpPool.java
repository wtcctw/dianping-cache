package com.dianping.cache.scale.instance.docker.paasbean;

import java.util.ArrayList;
import java.util.List;

public class IpPool {
	private long id;

	private Integer mask;

	private String gateway;

	private List<String> ips = new ArrayList<String>();

	public IpPool() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Integer getMask() {
		return mask;
	}

	public void setMask(Integer mask) {
		this.mask = mask;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public List<String> getIps() {
		return ips;
	}

	public void setIps(List<String> ips) {
		this.ips = ips;
	}

}
