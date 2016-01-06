package com.dianping.cache.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.dianping.cache.controller.dto.RedisDashBoardData;
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

	private String currentCluster;

	@RequestMapping(value = "/redis/dashboard", method = RequestMethod.GET)
	public ModelAndView viewClusterDashBoard(){
		return new ModelAndView("monitor/redisdashboard",createViewMap());
	}
	
	@RequestMapping(value = "/redis/dashboardinfo", method = RequestMethod.GET)
	@ResponseBody
	public List<RedisClusterData> getDashboard(){
		return RedisDataUtil.getClusterData();
	}

	@RequestMapping(value = "/redis/dashboard/data")
	@ResponseBody
	public RedisDashBoardData getRedisDashboard(){
		return RedisDataUtil.getRedisDashBoardData();
	}
	
	@RequestMapping(value = "/redis/serverinfo", method = RequestMethod.GET)
	public ModelAndView viewRedisServerInfo(){
		subside = "dashboard";
		return new ModelAndView("monitor/redisserverinfo",createViewMap());
	}
	
	@RequestMapping(value = "/redis/serverinfodata", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getRedisServerInfo(String address){
		return RedisDataUtil.getRedisServerData(address);
	}
	
	@RequestMapping(value = "/redis", method = RequestMethod.GET)
	public ModelAndView redis(){
		subside = "redis";
		return new ModelAndView("cluster/redis",createViewMap());
	}

	@RequestMapping(value = "/redis/{cluster}")
	public ModelAndView getRedisDetail(@PathVariable("cluster") String cluster){
		subside = "redis";
		currentCluster = cluster;
		return new ModelAndView("cluster/redisdetail",createViewMap());
	}
	@RequestMapping(value = "/redis/detail")
	@ResponseBody
	public Map<String, Object> getRedisDetailData(){
		return RedisDataUtil.getRedisDetailData(currentCluster);
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
		return RedisDataUtil.getClusterData();
	}


	@Override
	protected String getSide() {
		return "cluster";
	}

	@Override
	public String getSubSide() {
		return subside;
	}

}
