package com.dianping.cache.entity;

import java.io.Serializable;

public class ServerCluster implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6284449694724201749L;

	private int id;
	
	private int serverId;
	
	private String cluster;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	
	
}
