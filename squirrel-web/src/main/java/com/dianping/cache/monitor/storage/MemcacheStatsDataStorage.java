package com.dianping.cache.monitor.storage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import com.dianping.cache.entity.MemcacheStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.monitor.MemcachedClientFactory;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerService;
import com.dianping.combiz.spring.context.SpringLocator;

public class MemcacheStatsDataStorage extends AbstractStatsDataStorage{
	
	public static boolean START_MS = false;
	
	private static final int DEFAULT_PORT = 11211;

	private ServerService serverService;
	
	private MemcacheStatsService memcacheStatsService;
	
	private ExecutorService pool;

	
	public MemcacheStatsDataStorage(){
		init();
		scheduled.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				storage();
			}
			
		}, getStoragerInterval(), getStoragerInterval(), TimeUnit.SECONDS);
	}
	protected void init(){
		pool = Executors.newFixedThreadPool(5);
		serverService = SpringLocator.getBean("serverService");
		memcacheStatsService = SpringLocator.getBean("memcacheStatsService");
	}
	
	private void storage(){
		if(!START_MS || !isMaster()){
			return;
		}
		List<Server> serverList = serverService.findAllMemcachedServers();
		for(Server server : serverList){
			pool.submit(new InsertData(server));
		}
	}
	
	private MemcacheStats processStats(Map<String, String> stats) {
		int uptime = Integer.parseInt(stats.get("uptime"));
		long curr_time = System.currentTimeMillis()/1000;
		int total_connections = Integer.parseInt(stats.get("total_connections"));
		int curr_connections = Integer.parseInt(stats.get("curr_connections"));
		int curr_items = Integer.parseInt(stats.get("curr_items"));
		long cmd_set = Long.parseLong(stats.get("cmd_set"));
		long get_hits = Long.parseLong(stats.get("get_hits"));
		long get_misses = Long.parseLong(stats.get("get_misses"));
		long limit_maxbytes = Long.parseLong(stats.get("limit_maxbytes"));
		long delete_hits = Long.parseLong(stats.get("delete_hits"));
		long delete_misses = Long.parseLong(stats.get("delete_misses"));
		long evictions = Long.parseLong(stats.get("evictions"));
		long bytes_read = Long.parseLong(stats.get("bytes_read"));
		long bytes_written = Long.parseLong(stats.get("bytes_written"));
		long bytes = Long.parseLong(stats.get("bytes"));
		
		MemcacheStats msData = new MemcacheStats();
		msData.setUptime(uptime);
		msData.setCurr_time(curr_time);
		msData.setTotal_conn(total_connections);
		msData.setCurr_conn(curr_connections);
		msData.setCurr_items(curr_items);
		msData.setCmd_set(cmd_set);
		msData.setGet_hits(get_hits);
		msData.setGet_misses(get_misses);
		msData.setLimit_maxbytes(limit_maxbytes);
		msData.setDelete_hits(delete_hits);
		msData.setDelete_misses(delete_misses);
		msData.setEvictions(evictions);
		msData.setBytes_read(bytes_read);
		msData.setBytes_written(bytes_written);
		msData.setBytes(bytes);
		return msData;
	}

	@Override
	public int getStoragerInterval() {
		return super.getStoragerInterval();
	}

	@Override
	public void setStoragerInterval(int storagerInterval) {
		super.setStoragerInterval(storagerInterval);
	}
    private static ServerInfo parseServer(String server) {
        ServerInfo si = new ServerInfo();
        int idx = server.indexOf(':');
        if(idx == -1) {
            si.ip = server;
            si.port = DEFAULT_PORT;
        } else {
            si.ip = server.substring(0, idx);
            si.port = Integer.parseInt(server.substring(idx+1));
        }
        return si;
    }
    
    static class ServerInfo {
        String ip;
        int port;
    }
    
    private class InsertData implements Runnable{
    	private Server server;
    	
    	public InsertData(){
    	}
    	public InsertData(Server server){
    		this.server = server;
    	}
		@Override
		public void run() {
			try {
				 //getclient
				 MemcachedClient mc = MemcachedClientFactory.getMemcachedClient(server.getAddress());
				 //getStats
				 ServerInfo serverInfo = parseServer(server.getAddress());
				 Map<String, String> stats = mc.stats(new InetSocketAddress(serverInfo.ip, serverInfo.port),1000);
				 
				 //process data
				 MemcacheStats msData = processStats(stats);
				 msData.setServerId(server.getId());
				 //insert
				 memcacheStatsService.insert(msData);
				 
			} catch (IOException e) {
				logger.error("Connect " + server.getAddress() +" with IOException ! when storage statsdata");
			} catch (MemcachedException e) {
				logger.error("Connect " + server.getAddress() +" with MemcachedException !when storage statsdata");
			} catch (InterruptedException e) {
				logger.error("Connect " + server.getAddress() +" with InterruptedException !when storage statsdata");
			} catch (TimeoutException e) {
				logger.error("Connect " + server.getAddress() +" with TimeoutException !when storage statsdata");
			} catch (RuntimeException e){
				logger.error("RuntimeException"+e);
			}
		}
		public Server getServer() {
			return server;
		}
		public void setServer(Server server) {
			this.server = server;
		}
    	
    }
}
