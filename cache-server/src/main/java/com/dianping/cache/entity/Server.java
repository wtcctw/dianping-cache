package com.dianping.cache.entity;

import java.io.Serializable;

public class Server implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6652055359919798510L;

	private int id;
	
	private String address;
	
	private String appId;
	
	private String instanceId;
	
	private int type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
}
