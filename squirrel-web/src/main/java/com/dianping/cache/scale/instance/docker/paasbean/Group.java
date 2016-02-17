package com.dianping.cache.scale.instance.docker.paasbean;

import java.util.ArrayList;
import java.util.List;

public class Group {
   private String cpuSet;

   private Integer mode;

   private Double used;

   private List<Integer> cores = new ArrayList<Integer>();
	
	
	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public Double getUsed() {
		return used;
	}

	public void setUsed(Double used) {
		this.used = used;
	}

	public List<Integer> getCores() {
		return cores;
	}

	public void setCores(List<Integer> cores) {
		this.cores = cores;
	}

	public String getCpuSet() {
	   return cpuSet;
   }

	public void setCpuSet(String cpuSet) {
	   this.cpuSet = cpuSet;
   }

}
