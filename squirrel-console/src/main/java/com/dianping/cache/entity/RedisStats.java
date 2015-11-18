package com.dianping.cache.entity;

import java.io.Serializable;

public class RedisStats  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6982505979154110644L;
	
	private int id;
	
	private int serverId;//对应的机器ip
	
	private int memory_used;

	private long curr_time;
	
	private int total_commands;
	
	private int total_connections;
	
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

	public int getMemory_used() {
		return memory_used;
	}

	public void setMemory_used(int memory_used) {
		this.memory_used = memory_used;
	}

	public long getCurr_time() {
		return curr_time;
	}

	public void setCurr_time(long curr_time) {
		this.curr_time = curr_time;
	}

	public int getTotal_commands() {
		return total_commands;
	}

	public void setTotal_commands(int total_commands) {
		this.total_commands = total_commands;
	}

	public int getTotal_connections() {
		return total_connections;
	}

	public void setTotal_connections(int total_connections) {
		this.total_connections = total_connections;
	}
	

}
