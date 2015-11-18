package com.dianping.cache.autoscale;


public interface AutoScale {
	
	int scaleUp(AppId appId,int number);
	
	boolean scaleDown(String address);
	
	Result operation(String appid,int operateid);

	Result getValue(int operateid,Result value);

	void destroy(Result value);

	Result getValue(int operateid);
}
