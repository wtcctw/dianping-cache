package com.dianping.cache.monitor.statsdata;

import java.util.List;

import com.dianping.cache.entity.ServerStats;

public class ServerStatsData {
	
	private long startTime;
	
	private Float[] process_load;
	
	private Double[] net_in;// set secs
	
	private Double[] net_out;// get secs
	
	private Long[] mem_total;
	
	private Long[] mem_used;
	
	private Float[] icmp_loss;
	
	private Integer[] retransmission;
	
	
	public ServerStatsData(){
	}
	
	public ServerStatsData(List<ServerStats> stats){
		
		init(stats);
		for(int i = 0; i < stats.size(); i++){
			process_load[i] = stats.get(i).getProcess_load();
			net_in[i] = stats.get(i).getNet_in();
			net_out[i] = stats.get(i).getNet_out();
			mem_total[i] = stats.get(i).getMem_total();
			mem_used[i] = stats.get(i).getMem_used();
			icmp_loss[i] = stats.get(i).getIcmp_loss();
			retransmission[i] = stats.get(i).getRetransmission();
		}
	}
	
	private void init(List<ServerStats> stats){
		if(stats == null || stats.size() <= 1)
			throw new IllegalArgumentException("statsdata is null");
		int length = stats.size();
		startTime = stats.get(0).getCurr_time();
		process_load = new Float[length];
		net_in = new Double[length];
		net_out = new Double[length];
		mem_total = new Long[length];
		mem_used = new Long[length];
		icmp_loss = new Float[length];
		retransmission = new Integer[length];
	}
	

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Float[] getProcess_load() {
		return process_load;
	}

	public void setProcess_load(Float[] process_load) {
		this.process_load = process_load;
	}

	public Double[] getNet_in() {
		return net_in;
	}

	public void setNet_in(Double[] net_in) {
		this.net_in = net_in;
	}

	public Double[] getNet_out() {
		return net_out;
	}

	public void setNet_out(Double[] net_out) {
		this.net_out = net_out;
	}

	public Long[] getMem_total() {
		return mem_total;
	}

	public void setMem_total(Long[] mem_total) {
		this.mem_total = mem_total;
	}

	public Long[] getMem_used() {
		return mem_used;
	}

	public void setMem_used(Long[] mem_used) {
		this.mem_used = mem_used;
	}

	public Float[] getIcmp_loss() {
		return icmp_loss;
	}

	public void setIcmp_loss(Float[] icmp_loss) {
		this.icmp_loss = icmp_loss;
	}

	public Integer[] getRetransmission() {
		return retransmission;
	}

	public void setRetransmission(Integer[] retransmission) {
		this.retransmission = retransmission;
	}
	
	
	
	
	
}
