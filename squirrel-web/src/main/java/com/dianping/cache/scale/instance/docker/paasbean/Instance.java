package com.dianping.cache.scale.instance.docker.paasbean;

import java.util.ArrayList;
import java.util.List;

public class Instance {
	private String id;

	private Integer status;

	private String ip;

	private Integer mask;

	private String gateway;

	private String token;

	private String groupId;

	private Double weightInGroup = 0.0d;

	private List<Integer> cores = new ArrayList<Integer>();

	private App app;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Double getWeightInGroup() {
		return weightInGroup;
	}

	public void setWeightInGroup(Double weightInGroup) {
		this.weightInGroup = weightInGroup;
	}

	public List<Integer> getCores() {
		return cores;
	}

	public void setCores(List<Integer> cores) {
		this.cores = cores;
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

}
