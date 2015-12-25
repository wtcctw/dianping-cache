package com.dianping.cache.scale.instance;

import java.util.ArrayList;
import java.util.List;

public class Result {
	
	private int status=100;
	
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

	public int getNeed() {
		return need;
	}

	public void setNeed(int need) {
		this.need = need;
	}

	public AppId getAppId() {
		return appId;
	}

	public void setAppId(AppId appId) {
		this.appId = appId;
	}
	
}
