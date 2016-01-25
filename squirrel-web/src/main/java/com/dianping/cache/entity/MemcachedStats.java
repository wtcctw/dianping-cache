package com.dianping.cache.entity;

import com.dianping.squirrel.monitor.data.Stats;

import java.io.Serializable;

public class MemcachedStats implements Serializable,Stats{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4872649790508085451L;

	private int id;
	
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getUptime() {
		return uptime;
	}

	public void setUptime(int uptime) {
		this.uptime = uptime;
	}

	public int getTotal_conn() {
		return total_conn;
	}

	public void setTotal_conn(int total_conn) {
		this.total_conn = total_conn;
	}

	public int getCurr_conn() {
		return curr_conn;
	}

	public void setCurr_conn(int curr_conn) {
		this.curr_conn = curr_conn;
	}

	public int getCurr_items() {
		return curr_items;
	}

	public void setCurr_items(int curr_items) {
		this.curr_items = curr_items;
	}

	public long getGet_hits() {
		return get_hits;
	}

	public void setGet_hits(long get_hits) {
		this.get_hits = get_hits;
	}

	public long getGet_misses() {
		return get_misses;
	}

	public void setGet_misses(long get_misses) {
		this.get_misses = get_misses;
	}

	public long getBytes_read() {
		return bytes_read;
	}

	public void setBytes_read(long bytes_read) {
		this.bytes_read = bytes_read;
	}

	public long getBytes_written() {
		return bytes_written;
	}

	public void setBytes_written(long bytes_written) {
		this.bytes_written = bytes_written;
	}

	public long getDelete_hits() {
		return delete_hits;
	}

	public void setDelete_hits(long delete_hits) {
		this.delete_hits = delete_hits;
	}

	public long getDelete_misses() {
		return delete_misses;
	}

	public void setDelete_misses(long delete_misses) {
		this.delete_misses = delete_misses;
	}

	public long getEvictions() {
		return evictions;
	}

	public void setEvictions(long evictions) {
		this.evictions = evictions;
	}

	public long getLimit_maxbytes() {
		return limit_maxbytes;
	}

	public void setLimit_maxbytes(long limit_maxbytes) {
		this.limit_maxbytes = limit_maxbytes;
	}

	public long getCmd_set() {
		return cmd_set;
	}

	public void setCmd_set(long cmd_set) {
		this.cmd_set = cmd_set;
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public long getCurr_time() {
		return curr_time;
	}

	public void setCurr_time(long curr_time) {
		this.curr_time = curr_time;
	}


	
}
