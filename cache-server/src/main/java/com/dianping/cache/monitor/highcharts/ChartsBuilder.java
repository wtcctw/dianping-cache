package com.dianping.cache.monitor.highcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dianping.cache.monitor.highcharts.HighChartsWrapper.PlotOption;
import com.dianping.cache.monitor.highcharts.HighChartsWrapper.PlotOptionSeries;
import com.dianping.cache.monitor.highcharts.HighChartsWrapper.Series;
import com.dianping.cache.monitor.statsdata.MemcachedStatsData;
import com.dianping.cache.monitor.statsdata.ServerStatsData;

public class ChartsBuilder {
	
	
	public static List<HighChartsWrapper> buildMemcachedStatsCharts(Map<String,MemcachedStatsData> data){
		List<HighChartsWrapper> result = new ArrayList<HighChartsWrapper>();
		long startTime = System.currentTimeMillis()/1000;
		for(Map.Entry<String,MemcachedStatsData> entry : data.entrySet()){
			if(entry.getValue() != null && entry.getValue().getStartTime() < startTime){
				startTime = entry.getValue().getStartTime();
			}
		}
		result.add(build0("Set/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getSetsDatas();
			};
		})));
		result.add(build0("Get/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getGetsDatas();
			};
		})));
//		result.add(build0("Get_miss/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
//			@Override
//			public  Number[] extract(MemcachedStatsData value){
//				return value.getGetMissDatas();
//			};
//		})));
//		result.add(build0("Hit/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
//			@Override
//			public  Number[] extract(MemcachedStatsData value){
//				return value.getHitDatas();
//			};
//		})));
		result.add(build0("HitRate/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getHitRate();
			};
		})));
		result.add(build0("Write_bytes/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getWritesDatas();
			};
		})));
		result.add(build0("Read_bytes/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getReadsDatas();
			};
		})));
		result.add(build0("Eviction/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getEvictionsDatas();
			};
		})));
		result.add(build0("Curr_connection/s",startTime,getData(data,new AbstractExtractData<MemcachedStatsData>(){
			@Override
			public  Number[] extract(MemcachedStatsData value){
				return value.getConnDatas();
			};
		})));
		return result;
	}
	
	public static List<HighChartsWrapper> buildServerStatsCharts(Map<String,ServerStatsData> data){
		List<HighChartsWrapper> result = new ArrayList<HighChartsWrapper>();
		long startTime = System.currentTimeMillis()/1000;
		for(Map.Entry<String,ServerStatsData> entry : data.entrySet()){
			if(entry.getValue() != null && entry.getValue().getStartTime() < startTime){
				startTime = entry.getValue().getStartTime();
			}
		}
		result.add(build0("Net_in/s",startTime,getData(data,new AbstractExtractData<ServerStatsData>(){
			@Override
			public  Number[] extract(ServerStatsData value){
				return value.getNet_in();
			};
		})));
		result.add(build0("Net_out/s",startTime,getData(data,new AbstractExtractData<ServerStatsData>(){
			@Override
			public  Number[] extract(ServerStatsData value){
				return value.getNet_out();
			};
		})));
		result.add(build0("Load/s",startTime,getData(data,new AbstractExtractData<ServerStatsData>(){
			@Override
			public  Number[] extract(ServerStatsData value){
				return value.getProcess_load();
			};
		})));
		result.add(build0("Retransmission/s",startTime,getData(data,new AbstractExtractData<ServerStatsData>(){
			@Override
			public  Number[] extract(ServerStatsData value){
				return value.getRetransmission();
			};
		})));
		result.add(build0("Icmp_loss/s",startTime,getData(data,new AbstractExtractData<ServerStatsData>(){
			@Override
			public  Number[] extract(ServerStatsData value){
				return value.getIcmp_loss();
			};
		})));
		return result;
	}
	
	private static HighChartsWrapper build0(String title, long startTime, Map<String, Number[]> data) {
		HighChartsWrapper charts = new HighChartsWrapper();
		charts.setTitle(title);
		Series[] series = new Series[data.size()];
		int index = 0;
		for(Map.Entry<String, Number[]> entry : data.entrySet()){
			Series s = new Series();
			s.setData(entry.getValue());
			s.setName(entry.getKey());
			series[index++] = s;
		}
		PlotOption plotOption = new PlotOption();
		PlotOptionSeries pos = new PlotOptionSeries();

		pos.setPointStart(startTime*1000);
		pos.setPointInterval(30000L);
		plotOption.setSeries(pos);
		
		charts.setTitle(title);
		charts.setPlotOption(plotOption);
		charts.setSeries(series);
		
		return charts;
	}

	private static<T> Map<String,Number[]> getData(Map<String,T> data,ExtractData<T> ed){
		return ed.getData(data);
	}

}

