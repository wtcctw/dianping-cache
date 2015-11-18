package com.dianping.cache.monitor.highcharts;

import java.util.Map;

public interface ExtractData<T> {
	
	public Map<String,Number[]> getData(Map<String,T> data);
	
	public Number[] extract(T value);
}
