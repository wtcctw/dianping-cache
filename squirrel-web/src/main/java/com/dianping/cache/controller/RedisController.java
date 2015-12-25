package com.dianping.cache.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.dianping.cache.controller.dto.RedisScaleParams;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisScaler;
import com.dianping.cache.scale.exceptions.ScaleException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.monitor.highcharts.ChartsBuilder;
import com.dianping.cache.monitor.highcharts.HighChartsWrapper;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.monitor.statsdata.RedisStatsData;
import com.dianping.cache.service.RedisStatsService;


@Controller
public class RedisController extends AbstractCacheController{
	
	
	@Resource(name = "redisStatsService")
	private RedisStatsService redisStatsService;
	
	private String subside;

	@RequestMapping(value = "/redis/dashboard", method = RequestMethod.GET)
	public ModelAndView viewClusterDashBoard(){
		return new ModelAndView("monitor/redisdashboard",createViewMap());
	}
	
	@RequestMapping(value = "/redis/dashboardinfo", method = RequestMethod.GET)
	@ResponseBody
	public List<RedisClusterData> getDashboard(){
		return RedisDashBoardUtil.getClusterData();
	}
	
	@RequestMapping(value = "/redis/serverinfo", method = RequestMethod.GET)
	public ModelAndView viewRedisServerInfo(){
		subside = "dashboard";
		return new ModelAndView("monitor/redisserverinfo",createViewMap());
	}
	
	@RequestMapping(value = "/redis/serverinfodata", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getRedisServerInfo(String address){
		return RedisDashBoardUtil.getRedisServerData(address);
	}
	
	@RequestMapping(value = "/redis/scale", method = RequestMethod.GET)
	public ModelAndView redisScale(){
		
		subside = "dashboard";
		return new ModelAndView("cache/redisscale",createViewMap());
	}

	@RequestMapping(value = "/redis/historydata", method = RequestMethod.GET)
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistoryData(String address,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES))/1000;
		long end = endTime/1000;
		List<RedisStats> data = redisStatsService.findByServerWithInterval(address, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}


	@RequestMapping(value = "/redis/addslave", method = RequestMethod.POST)
	@ResponseBody
	public void addSlave(@RequestBody RedisScaleParams redisScaleParams) throws ScaleException {
		RedisScaler.addSlave(redisScaleParams.getCluster(),redisScaleParams.getMasterAddress());
	}

	@RequestMapping(value = "/redis/deleteslave", method = RequestMethod.POST)
	@ResponseBody
	public void delSlave(@RequestBody RedisScaleParams redisScaleParams) throws ScaleException{
		RedisScaler.removeSlave(redisScaleParams.getCluster(),redisScaleParams.getSlaveAddress());
	}

	@RequestMapping(value = "/redis/refreshcache")
	@ResponseBody
	public List<RedisClusterData> refreshClusterCache(String cluster) throws ScaleException{
		RedisManager.refreshCache(cluster);
		return RedisDashBoardUtil.getClusterData();
	}


	@Override
	protected String getSide() {
		return "monitor";
	}

	@Override
	public String getSubSide() {
		return "rdashboard";
	}

}
