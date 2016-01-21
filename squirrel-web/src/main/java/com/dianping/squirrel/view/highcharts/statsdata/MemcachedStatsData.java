package com.dianping.squirrel.view.highcharts.statsdata;

import com.dianping.cache.entity.MemcachedStats;

import java.util.List;

public class MemcachedStatsData {
	
	private int length;
	
	private long startTime;
	
	private long max_memory;
	
	private Long[] setsDatas;// set secs
	
	private Long[] getsDatas;// get secs
	
	private Long[] getMissDatas; // miss secs
	
	private Long[] hitDatas;
	
	private Float[] hitRate;
	
	private Long[] readsDatas; // read/s
	
	private Long[] writesDatas;
	
	private Integer[] connDatas;
	
	private Long[] evictionsDatas; 
	
	private Float[] usage;
	
	private Long[] bytes;
	
	public MemcachedStatsData(){
	}
	
	public MemcachedStatsData(List<MemcachedStats> stats){
		
		init(stats);
		
		for(int i = 0; i < length; i++){
			int interval = stats.get(i+1).getUptime() - stats.get(i).getUptime();
			if(interval <= 5 || interval >= 35)
				continue;
			setsDatas[i] = (stats.get(i+1).getCmd_set() - stats.get(i).getCmd_set())/interval;
			getsDatas[i] = (stats.get(i+1).getGet_hits() - stats.get(i).getGet_hits())/interval;
			getMissDatas[i] = (stats.get(i+1).getGet_misses() - stats.get(i).getGet_misses())/interval;
			hitDatas[i] = setsDatas[i] + getsDatas[i] + getMissDatas[i];
			if(getsDatas[i] > 0)
				hitRate[i] = (float) ((double)getsDatas[i]/(getsDatas[i] + getMissDatas[i]));
			else
				hitRate[i] = 1f;
			readsDatas[i] = (stats.get(i+1).getBytes_read() - stats.get(i).getBytes_read())/interval;
			writesDatas[i] = (stats.get(i+1).getBytes_written() - stats.get(i).getBytes_written())/interval;
			connDatas[i] = stats.get(i+1).getCurr_conn();
			evictionsDatas[i] = (stats.get(i+1).getEvictions() - stats.get(i).getEvictions())/interval;
			usage[i] = (float) (stats.get(i+1).getBytes() / stats.get(i+1).getLimit_maxbytes()); // 需要转换下  float
			bytes[i] = stats.get(i+1).getBytes()/1024/1024;
		}
	}
	
	private void init(List<MemcachedStats> stats){
		if(stats == null || stats.size() <= 1)
			throw new IllegalArgumentException("statsdata is null");
		
		this.length = stats.size() - 1;
		this.startTime = stats.get(0).getCurr_time();
		this.max_memory = stats.get(0).getLimit_maxbytes()/1024/1024;
		this.setsDatas = new Long[length];
		this.getsDatas = new Long[length];
		this.hitDatas = new Long[length];
		this.hitRate = new Float[length];
		this.getMissDatas = new Long[length];
		this.readsDatas = new Long[length];
		this.writesDatas = new Long[length];
		this.connDatas = new Integer[length];
		this.evictionsDatas = new Long[length];
		this.usage = new Float[length];
		this.bytes = new Long[length];
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public Long[] getSetsDatas() {
		return setsDatas;
	}

	public void setSetsDatas(Long[] setsDatas) {
		this.setsDatas = setsDatas;
	}

	public Long[] getGetsDatas() {
		return getsDatas;
	}

	public void setGetsDatas(Long[] getsDatas) {
		this.getsDatas = getsDatas;
	}

	public Long[] getGetMissDatas() {
		return getMissDatas;
	}

	public void setGetMissDatas(Long[] getMissDatas) {
		this.getMissDatas = getMissDatas;
	}

	public Long[] getReadsDatas() {
		return readsDatas;
	}

	public void setReadsDatas(Long[] readsDatas) {
		this.readsDatas = readsDatas;
	}

	public Long[] getWritesDatas() {
		return writesDatas;
	}

	public void setWritesDatas(Long[] writesDatas) {
		this.writesDatas = writesDatas;
	}

	public Integer[] getConnDatas() {
		return connDatas;
	}

	public void setConnDatas(Integer[] connDatas) {
		this.connDatas = connDatas;
	}

	public Long[] getEvictionsDatas() {
		return evictionsDatas;
	}

	public void setEvictionsDatas(Long[] evictionsDatas) {
		this.evictionsDatas = evictionsDatas;
	}

	public Float[] getUsage() {
		return usage;
	}

	public void setUsage(Float[] usage) {
		this.usage = usage;
	}

	public long getMax_memory() {
		return max_memory;
	}

	public void setMax_memory(long max_memory) {
		this.max_memory = max_memory;
	}

	public Long[] getBytes() {
		return bytes;
	}

	public void setBytes(Long[] bytes) {
		this.bytes = bytes;
	}

	public Long[] getHitDatas() {
		return hitDatas;
	}

	public void setHitDatas(Long[] hitDatas) {
		this.hitDatas = hitDatas;
	}

	public Float[] getHitRate() {
		return hitRate;
	}

	public void setHitRate(Float[] hitRate) {
		this.hitRate = hitRate;
	}
}
