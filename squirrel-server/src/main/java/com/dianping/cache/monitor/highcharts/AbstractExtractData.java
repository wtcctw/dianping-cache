package com.dianping.cache.monitor.highcharts;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExtractData<T> implements ExtractData<T>{
	@Override
	public Map<String,Number[]> getData(Map<String,T> data){
		Map<String,Number[]> result = new HashMap<String,Number[]>();
		for(Map.Entry<String, T> entry : data.entrySet()){
			result.put(entry.getKey(), extract(entry.getValue()));
		}
		return result;
	}	
}
