package com.dianping.squirrel.view.highcharts;

import java.util.Map;

public interface ExtractData<T> {
	
	Map<String,Number[]> getData(Map<String, T> data);
	
	Number[] extract(T value);
}
