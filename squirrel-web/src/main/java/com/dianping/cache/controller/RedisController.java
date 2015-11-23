package com.dianping.cache.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.autoscale.Result;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.monitor.highcharts.ChartsBuilder;
import com.dianping.cache.monitor.highcharts.HighChartsWrapper;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.monitor.statsdata.RedisStatsData;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.impl.RedisCluster;
import com.dianping.cache.scale.impl.RedisNode;
import com.dianping.cache.scale.impl.RedisScaler;
import com.dianping.cache.scale.impl.RedisServer;
import com.dianping.cache.scale.impl.RedisUtil;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.RedisStatsService;
import com.dianping.cache.service.ServerService;

import edu.emory.mathcs.backport.java.util.Arrays;

@Controller
public class RedisController extends AbstractCacheController{
	
	
	@Resource(name = "cacheConfigurationService")
	private CacheConfigurationService cacheConfigurationService;
	
	@Resource(name = "redisStatsService")
	private RedisStatsService redisStatsService;
	
	@Resource(name = "serverService")
	private ServerService serverService;
	
	private String subside;
	
	@RequestMapping(value = "/redis/clusterinfo", method = RequestMethod.GET)
	@ResponseBody
	public List<RedisNode> getClusterInfo(String cacheKey){
		CacheConfiguration config = cacheConfigurationService.find(cacheKey);
		String url = config.getServers();
		List<String> servers = parseServers(url);
		RedisCluster redisCluster = new RedisCluster(servers);
		try {
			redisCluster.loadClusterInfo();
		} catch (Exception e) {
			logger.error("Load cluster info error !"+e);
		}
		return redisCluster.getNodes();
	}
	
	@RequestMapping(value = "/redis/dashboard", method = RequestMethod.GET)
	public ModelAndView viewClusterDashBoard(HttpServletRequest request, HttpServletResponse response){
		return new ModelAndView("monitor/redisdashboard",createViewMap());
	}
	
	@RequestMapping(value = "/redis/dashboardinfo", method = RequestMethod.GET)
	@ResponseBody
	public List<RedisClusterData> getDashboard(){
		return RedisDashBoardUtil.getClusterData();
	}
	
	@RequestMapping(value = "/redis/serverinfo", method = RequestMethod.GET)
	public ModelAndView viewRedisServerInfo(HttpServletRequest request, HttpServletResponse response){
		
		subside = "dashboard";
		return new ModelAndView("monitor/redisserverinfo",createViewMap());
	}
	
	@RequestMapping(value = "/redis/serverinfodata", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getRedisServerInfo(String address){
		return RedisDashBoardUtil.getRedisServerData(address);
	}
	
	@RequestMapping(value = "/redis/scale", method = RequestMethod.GET)
	public ModelAndView redisScale(HttpServletRequest request, HttpServletResponse response){
		
		subside = "dashboard";
		return new ModelAndView("cache/redisscale",createViewMap());
	}
	
	
	@RequestMapping(value = "/redis/historydata", method = RequestMethod.GET)
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistoryData(String address,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS))/1000;
		long end = endTime/1000;
		List<HighChartsWrapper> charts = null;
		List<RedisStats> data = redisStatsService.findByServerWithInterval(address, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}
	
	
	private RedisStatsData convertStats(List<RedisStats> data) {
		// TODO Auto-generated method stub
		
		return null;
	}

	@RequestMapping(value = "/redis/addmaster", method = RequestMethod.POST)
	@ResponseBody
	public void addMaster(String cluster,String ip,int port) throws ScaleException{
		RedisServer master = new RedisServer(ip+":"+port);
		RedisNode node = new RedisNode();
		node.setMaster(master);
		
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String url = config.getServers();
		List<String> servers = parseServers(url);
		RedisScaler rs = new RedisScaler(servers, Arrays.asList(new String[]{ip+":"+port}));
		rs.scaleUp();
	}
	
	@RequestMapping(value = "/redis/applynodes", method = RequestMethod.POST)
	@ResponseBody
	public int applyNodes(String cluster,int number){
		String appId = getClusterAppId(cluster);
		return RedisUtil.applyNodes(appId, number);
	}
	
	
	@RequestMapping(value = "/redis/getresult", method = RequestMethod.POST)
	@ResponseBody
	public Result getResult(int operateId){
		return RedisUtil.getResult(operateId);
	}
	
	@RequestMapping(value = "/redis/destroy", method = RequestMethod.POST)
	public void destroy(String address){
		RedisUtil.destroy(address);
	}


	@RequestMapping(value = "/redis/des", method = RequestMethod.GET)
	public void des(String instanceId){
		RedisUtil.des(instanceId);
	}



	private String getClusterAppId(String cluster) {
		CacheConfiguration config = cacheConfigurationService.find(cluster);
		String url = config.getServers();
		List<String> servers = parseServers(url);
		Server server = serverService.findByAddress(servers.get(0));
		return server.getAppId();
	}

	@RequestMapping(value = "/redis/autoscaleup", method = RequestMethod.POST)
	@ResponseBody
	public int autoScaleUp(String cluster,String appid,int instances) throws ScaleException{
		return RedisUtil.scaleNode(cluster, instances, appid);
	}
	
	@RequestMapping(value = "/redis/autoaddslave", method = RequestMethod.POST)
	@ResponseBody
	public int autoScaleUpSlave(String master) throws ScaleException{
		return RedisUtil.scaleSlave(master);
	}
	
	@RequestMapping(value = "/redis/getscalestatus", method = RequestMethod.POST)
	@ResponseBody
	public int getScaleStatus(int scaleOperationId) throws ScaleException{
		return RedisUtil.getOperateStatus(scaleOperationId);
	}	
	
	@RequestMapping(value = "/redis/delmaster", method = RequestMethod.POST)
	@ResponseBody
	public int delMaster(String cluster,String address) throws ScaleException{
		return RedisUtil.deleteMaster(cluster, address);
	}
	
	@RequestMapping(value = "/redis/delslave", method = RequestMethod.POST)
	@ResponseBody
	public int delSlave(String cluster,String address) throws ScaleException{
		return RedisUtil.deleteSlave(cluster, address);
	}
	
	

	private List<String> parseServers(String url) {
		String URL_PREFIX = "redis-cluster://";
		if(url == null || !url.startsWith(URL_PREFIX)) {
	            return null;
	    }
		String servers = url.substring(URL_PREFIX.length(), url.indexOf('?'));
		
		List<String> serverList = new ArrayList<String>();
		String[] array = servers.split(",");
		for (String s : array) {
			serverList.add(s);
		}
		return serverList;
	}
	
	public static void main(String [] agr) throws ScaleException{
	
	}
	
	@Override
	protected String getSide() {
		// TODO Auto-generated method stub
		return "monitor";
	}

	@Override
	public String getSubSide() {
		// TODO Auto-generated method stub
		return "rdashboard";
	}

}
