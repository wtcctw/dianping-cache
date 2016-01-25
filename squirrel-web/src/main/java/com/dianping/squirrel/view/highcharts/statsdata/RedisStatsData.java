package com.dianping.squirrel.view.highcharts.statsdata;

import com.dianping.cache.entity.RedisStats;

import java.util.List;

public class RedisStatsData {
	private int length;
	
	private long startTime;
	
	private Long[] used_memory;
	
	private Integer[] total_connections;

	private Integer[] connected_clients;

	private Double[] input_kbps;

	private Double[] output_kbps;

	private Double[] used_cpu_sys;

	private Double[] used_cpu_user;

	private Double[] used_cpu_sys_children;

	private Double[] used_cpu_user_children;
	
	public RedisStatsData(List<RedisStats> data) {
		if (data != null && data.size() > 1) {
			length = data.size();
			startTime = data.get(0).getCurr_time();
			used_memory = new Long[length];
			total_connections = new Integer[length];
			connected_clients = new Integer[length];
			input_kbps = new Double[length];
			output_kbps = new Double[length];
			used_cpu_sys = new Double[length];
			used_cpu_sys_children = new Double[length];
			used_cpu_user = new Double[length];
			used_cpu_user_children = new Double[length];
			int index = 0;
			for (RedisStats stat : data) {
				used_memory[index] = stat.getMemory_used();
				total_connections[index] = stat.getTotal_connections();
				connected_clients[index] = stat.getConnected_clients();
				input_kbps[index] = stat.getInput_kbps();
				output_kbps[index] = stat.getOutput_kbps();
				used_cpu_sys[index] = stat.getUsed_cpu_sys();
				used_cpu_sys_children[index] = stat.getUsed_cpu_sys_children();
				used_cpu_user[index] = stat.getUsed_cpu_user();
				used_cpu_user_children[index] = stat.getUsed_cpu_user_children();
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

	public Long[] getUsed_memory() {
		return used_memory;
	}

	public void setUsed_memory(Long[] used_memory) {
		this.used_memory = used_memory;
	}

	public Integer[] getTotal_connections() {
		return total_connections;
	}

	public void setTotal_connections(Integer[] total_connections) {
		this.total_connections = total_connections;
	}

	public Integer[] getConnected_clients() {
		return connected_clients;
	}

	public void setConnected_clients(Integer[] connected_clients) {
		this.connected_clients = connected_clients;
	}

	public Double[] getInput_kbps() {
		return input_kbps;
	}

	public void setInput_kbps(Double[] input_kbps) {
		this.input_kbps = input_kbps;
	}

	public Double[] getOutput_kbps() {
		return output_kbps;
	}

	public void setOutput_kbps(Double[] output_kbps) {
		this.output_kbps = output_kbps;
	}

	public Double[] getUsed_cpu_sys() {
		return used_cpu_sys;
	}

	public void setUsed_cpu_sys(Double[] used_cpu_sys) {
		this.used_cpu_sys = used_cpu_sys;
	}

	public Double[] getUsed_cpu_user() {
		return used_cpu_user;
	}

	public void setUsed_cpu_user(Double[] used_cpu_user) {
		this.used_cpu_user = used_cpu_user;
	}

	public Double[] getUsed_cpu_sys_children() {
		return used_cpu_sys_children;
	}

	public void setUsed_cpu_sys_children(Double[] used_cpu_sys_children) {
		this.used_cpu_sys_children = used_cpu_sys_children;
	}

	public Double[] getUsed_cpu_user_children() {
		return used_cpu_user_children;
	}

	public void setUsed_cpu_user_children(Double[] used_cpu_user_children) {
		this.used_cpu_user_children = used_cpu_user_children;
	}
}
