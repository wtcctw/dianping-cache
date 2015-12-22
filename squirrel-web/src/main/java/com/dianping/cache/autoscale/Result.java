package com.dianping.cache.autoscale;

import java.util.ArrayList;
import java.util.List;

public class Result {
	
	private int status;
	
	private int need;
	
	private AppId appId;
	
	private List<Instance> instances = new ArrayList<Instance>();
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
	
	public void setAppId(String appId){
		for(Instance ins : instances){
			ins.setAppid(AppId.valueOf(appId).toString());
		}
	}

	public int getNeed() {
		return need;
	}

	public void setNeed(int need) {
		this.need = need;
	}

	public AppId getAppId() {
		return appId;
	}

	public void setAppId(AppId appid) {
		this.appId = appid;
	}
	
}
