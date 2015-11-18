package com.dianping.cache.monitor.statsdata;

import java.util.List;

import com.dianping.cache.entity.RedisStats;

public class RedisStatsData {
	private int length;
	
	private long startTime;
	
	private Integer[] used_memory;
	
	private Integer[] total_connections;
	
	private Integer[] total_commands;
	
	public RedisStatsData(){}

	public RedisStatsData(List<RedisStats> data) {
		if (data != null && data.size() > 1) {
			length = data.size();
			startTime = data.get(0).getCurr_time();
			used_memory = new Integer[length];
			total_connections = new Integer[length];
			total_commands = new Integer[length];
			int index = 0;
			for (RedisStats stat : data) {
				used_memory[index] = stat.getMemory_used();
				total_connections[index] = stat.getTotal_connections();
				total_commands[index] = stat.getTotal_commands();
				index++;
			}
		}
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Integer[] getUsed_memory() {
		return used_memory;
	}

	public void setUsed_memory(Integer[] used_memory) {
		this.used_memory = used_memory;
	}

	public Integer[] getTotal_connections() {
		return total_connections;
	}

	public void setTotal_connections(Integer[] total_connections) {
		this.total_connections = total_connections;
	}

	public Integer[] getTotal_commands() {
		return total_commands;
	}

	public void setTotal_commands(Integer[] total_commands) {
		this.total_commands = total_commands;
	}
	
}
