package com.dianping.cache.scale1.instance;

public interface Apply {
	int apply(AppId appId, int number);
	void destroy(String address);
	void destroy(String appId,String... instanceId);
	void destroy(Result result);
	Result getValue(int operationId);
}
