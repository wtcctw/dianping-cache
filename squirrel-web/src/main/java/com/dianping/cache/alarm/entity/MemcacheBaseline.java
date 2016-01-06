package com.dianping.cache.alarm.entity;

import java.io.Serializable;
import java.util.Date;

public class MemcacheBaseline implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4872649790508085451L;

	private int id;

	private String baseline_name;

	private int serverId;//对应的机器ip
	
	private int uptime;//服务器运行秒数
	
	private long curr_time;//当前时间

	private int total_conn;
	
	private int curr_conn;
	
	private int curr_items;
	
	private long cmd_set;
	
	private long get_hits;
		
	private long get_misses;
	
	private long bytes_read;
	
	private long bytes_written;	
	
	private long delete_hits;
	
	private long delete_misses;
	
	private long evictions;
	
	private long limit_maxbytes;
	
	private long bytes;

	private int taskId;

	private Date updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBaseline_name() {
		return baseline_name;
	}

	public MemcacheBaseline setBaseline_name(String baseline_name) {
		this.baseline_name = baseline_name;
		return this;
	}

	public int getServerId() {
		return serverId;
	}

	public MemcacheBaseline setServerId(int serverId) {
		this.serverId = serverId;
		return this;
	}

	public int getUptime() {
		return uptime;
	}

	public MemcacheBaseline setUptime(int uptime) {
		this.uptime = uptime;
		return this;
	}

	public int getTotal_conn() {
		return total_conn;
	}

	public MemcacheBaseline setTotal_conn(int total_conn) {
		this.total_conn = total_conn;
		return this;
	}

	public int getCurr_conn() {
		return curr_conn;
	}

	public MemcacheBaseline setCurr_conn(int curr_conn) {
		this.curr_conn = curr_conn;
		return this;
	}

	public int getCurr_items() {
		return curr_items;
	}

	public MemcacheBaseline setCurr_items(int curr_items) {
		this.curr_items = curr_items;
		return this;
	}

	public long getGet_hits() {
		return get_hits;
	}

	public MemcacheBaseline setGet_hits(long get_hits) {
		this.get_hits = get_hits;
		return this;
	}

	public long getGet_misses() {
		return get_misses;
	}

	public MemcacheBaseline setGet_misses(long get_misses) {
		this.get_misses = get_misses;
		return this;
	}

	public long getBytes_read() {
		return bytes_read;
	}

	public MemcacheBaseline setBytes_read(long bytes_read) {
		this.bytes_read = bytes_read;
		return this;
	}

	public long getBytes_written() {
		return bytes_written;
	}

	public MemcacheBaseline setBytes_written(long bytes_written) {
		this.bytes_written = bytes_written;
		return this;
	}

	public long getDelete_hits() {
		return delete_hits;
	}

	public MemcacheBaseline setDelete_hits(long delete_hits) {
		this.delete_hits = delete_hits;
		return this;
	}

	public long getDelete_misses() {
		return delete_misses;
	}

	public MemcacheBaseline setDelete_misses(long delete_misses) {
		this.delete_misses = delete_misses;
		return this;
	}

	public long getEvictions() {
		return evictions;
	}

	public MemcacheBaseline setEvictions(long evictions) {
		this.evictions = evictions;
		return this;
	}

	public long getLimit_maxbytes() {
		return limit_maxbytes;
	}

	public MemcacheBaseline setLimit_maxbytes(long limit_maxbytes) {
		this.limit_maxbytes = limit_maxbytes;
		return this;
	}

	public long getCmd_set() {
		return cmd_set;
	}

	public MemcacheBaseline setCmd_set(long cmd_set) {
		this.cmd_set = cmd_set;
		return this;
	}

	public long getBytes() {
		return bytes;
	}

	public MemcacheBaseline setBytes(long bytes) {
		this.bytes = bytes;
		return this;
	}

	public long getCurr_time() {
		return curr_time;
	}

	public MemcacheBaseline setCurr_time(long curr_time) {
		this.curr_time = curr_time;
		return this;
	}

	public int getTaskId() {
		return taskId;
	}

	public MemcacheBaseline setTaskId(int taskId) {
		this.taskId = taskId;
		return this;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public MemcacheBaseline setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
		return this;
	}
}
