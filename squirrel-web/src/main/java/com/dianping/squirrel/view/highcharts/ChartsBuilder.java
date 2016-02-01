package com.dianping.squirrel.view.highcharts;

import com.dianping.cache.entity.RedisStats;
import com.dianping.squirrel.view.highcharts.HighChartsWrapper.PlotOption;
import com.dianping.squirrel.view.highcharts.HighChartsWrapper.PlotOptionSeries;
import com.dianping.squirrel.view.highcharts.HighChartsWrapper.Series;
import com.dianping.squirrel.view.highcharts.statsdata.MemcachedStatsData;
import com.dianping.squirrel.view.highcharts.statsdata.RedisStatsData;
import com.dianping.squirrel.view.highcharts.statsdata.ServerStatsData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
	
	public static List<HighChartsWrapper> buildRedisStatsCharts(RedisStatsData data){
		List<HighChartsWrapper> result = new ArrayList<HighChartsWrapper>();
		if(data != null){
			long startTime = data.getStartTime();
			//result.add(build1("Used_Memory_Dayly",startTime,data.getUsed_memory()));
			result.add(build1("Used_Memory",startTime,data.getUsed_memory()));
			result.add(build1("QPS",startTime,data.getQps()));
			result.add(build1("input_kbps",startTime,data.getInput_kbps()));
			result.add(build1("output_kbps",startTime,data.getOutput_kbps()));
			result.add(build1("total_connections_received",startTime,data.getTotal_connections()));
			result.add(build1("connected_clients",startTime,data.getConnected_clients()));
			result.add(build1("used_cpu_sys",startTime,data.getUsed_cpu_sys()));
			result.add(build1("used_cpu_user",startTime,data.getUsed_cpu_user()));
		}
		return result;
	}

	public static HighChartsWrapper buildPeriodCharts(List<RedisStats> statsList,int period,long endTime){
		HighChartsWrapper charts = new HighChartsWrapper();
		charts.setTitle("Period Data For every " + period + " Days");
		Series[] series = new Series[1];
		series[0] = new Series();
		Number[] data = new Number[statsList.size()];
		int index = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND, 0);
		long startTime = calendar.getTimeInMillis() - 7*TimeUnit.MILLISECONDS.convert(period,TimeUnit.DAYS);
		for(RedisStats stats : statsList){
			if(stats != null){
				data[index++] = stats.getMemory_used();
			}else{
				data[index++] = null;
			}
		}

		series[0].setData(data);
		series[0].setName("Server");
		PlotOption plotOption = new PlotOption();
		PlotOptionSeries pos = new PlotOptionSeries();

		pos.setPointStart(startTime);
		pos.setPointInterval(TimeUnit.MILLISECONDS.convert(period,TimeUnit.DAYS));
		plotOption.setSeries(pos);

		charts.setyAxisTitle("");
		charts.setPlotOption(plotOption);
		charts.setSeries(series);
		return charts;
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
	
	private static HighChartsWrapper build1(String title, long startTime, Number[] data) {
		HighChartsWrapper charts = new HighChartsWrapper();
		charts.setTitle(title);
		Series[] series = new Series[1];
		series[0] = new Series();
		series[0].setData(data);
		series[0].setName("RedisServer");
		PlotOption plotOption = new PlotOption();
		PlotOptionSeries pos = new PlotOptionSeries();

		pos.setPointStart(startTime*1000);
		pos.setPointInterval(30000L);
		plotOption.setSeries(pos);
		
		charts.setTitle(title);
		charts.setyAxisTitle("");
		charts.setPlotOption(plotOption);
		charts.setSeries(series);
		
		return charts;
	}

	private static<T> Map<String,Number[]> getData(Map<String,T> data,ExtractData<T> ed){
		return ed.getData(data);
	}

}

