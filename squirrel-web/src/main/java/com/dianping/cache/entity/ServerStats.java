package com.dianping.cache.entity;

import com.dianping.squirrel.monitor.data.Stats;

import java.io.Serializable;

public class ServerStats implements Serializable,Stats{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7752957187141714897L;

	private int id;
	
	private int serverId;//对应的机器ip
	
	private int curr_time;
	
	//
	private float process_load;
	
	private double net_in;
	
	private double net_out;
	
	private long mem_total;
	
	private long mem_used;
	
	private float icmp_loss;
	
	private int retransmission;

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

	public int getCurr_time() {
		return curr_time;
	}

	public void setCurr_time(int curr_time) {
		this.curr_time = curr_time;
	}

	public float getProcess_load() {
		return process_load;
	}

	public void setProcess_load(float process_load) {
		this.process_load = process_load;
	}

	public double getNet_in() {
		return net_in;
	}

	public void setNet_in(double net_in) {
		this.net_in = net_in;
	}

	public double getNet_out() {
		return net_out;
	}

	public void setNet_out(double net_out) {
		this.net_out = net_out;
	}

	public long getMem_total() {
		return mem_total;
	}

	public void setMem_total(long mem_total) {
		this.mem_total = mem_total;
	}

	public long getMem_used() {
		return mem_used;
	}

	public void setMem_used(long mem_used) {
		this.mem_used = mem_used;
	}

	public float getIcmp_loss() {
		return icmp_loss;
	}

	public void setIcmp_loss(float icmp_loss) {
		this.icmp_loss = icmp_loss;
	}

	public int getRetransmission() {
		return retransmission;
	}

	public void setRetransmission(int retransmission) {
		this.retransmission = retransmission;
	}
	
	
}
