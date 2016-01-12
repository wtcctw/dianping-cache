package com.dianping.cache.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;


import com.dianping.cache.controller.dto.RedisDashBoardData;
import com.dianping.cache.controller.dto.RedisReshardParams;
import com.dianping.cache.controller.dto.RedisScaleParams;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.CacheKeyConfiguration;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisScaler;
import com.dianping.cache.scale.cluster.redis.ReshardPlan;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.cache.service.ReshardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.monitor.highcharts.ChartsBuilder;
import com.dianping.cache.monitor.highcharts.HighChartsWrapper;
import com.dianping.cache.monitor.statsdata.RedisClusterData;
import com.dianping.cache.monitor.statsdata.RedisStatsData;
import com.dianping.cache.service.RedisService;
import com.dianping.squirrel.task.TaskManager;
import com.dianping.squirrel.task.RedisReshardTask;


@Controller
public class RedisController extends AbstractSidebarController{


	@Autowired
	private RedisService redisService;

	@Autowired
	private ReshardService reshardService;

	@Autowired
	private CacheKeyConfigurationService cacheKeyConfigurationService;

	@Autowired
	private CacheConfigurationService cacheConfigurationService;

	private String subside;

	private String currentCluster;

	@RequestMapping(value = "/redis/dashboard")
	public ModelAndView viewClusterDashBoard(){
		return new ModelAndView("monitor/redisdashboard",createViewMap());
	}
	
	@RequestMapping(value = "/redis/dashboardinfo")
	@ResponseBody
	public List<RedisClusterData> getDashboard(){
		return RedisDataUtil.getClusterData();
	}

	@RequestMapping(value = "/redis/dashboard/data")
	@ResponseBody
	public RedisDashBoardData getRedisDashboard(){
		return RedisDataUtil.getRedisDashBoardData();
	}
	
	@RequestMapping(value = "/redis/serverinfo")
	public ModelAndView viewRedisServerInfo(){
		subside = "dashboard";
		return new ModelAndView("monitor/redisserverinfo",createViewMap());
	}
	
	@RequestMapping(value = "/redis/serverinfodata")
	@ResponseBody
	public Map<String, Object> getRedisServerInfo(String address){
		return RedisDataUtil.getRedisServerData(address);
	}
	
	@RequestMapping(value = "/redis")
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

	@RequestMapping(value = "/redis/{cluster}/edit")
	public ModelAndView edit(@PathVariable("cluster") String cluster){
		subside = "redis";
		currentCluster = cluster;
		return new ModelAndView("cluster/edit",createViewMap());
	}

	@RequestMapping(value = "/redis/editdata")
	@ResponseBody
	public CacheConfiguration editRedis(@RequestParam String swimlane){
		CacheConfiguration configuration = cacheConfigurationService.findWithSwimLane(currentCluster,swimlane);
		return configuration;
	}

	@RequestMapping(value = "/redis/detail")
	@ResponseBody
	public Map<String, Object> getRedisDetailData(){
		List<CacheKeyConfiguration> categorys = cacheKeyConfigurationService.findByCacheType(currentCluster);
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("data",RedisDataUtil.getRedisDetailData(currentCluster));
		result.put("categorys",categorys);
		return result;
	}

	@RequestMapping(value = "/redis/applications")
	@ResponseBody
	public List<String> applications(){
		return null;
	}

	@RequestMapping(value = "/redis/historydata")
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistoryData(String address,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES))/1000;
		long end = endTime/1000;
		List<RedisStats> data = redisService.findByServerWithInterval(address, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}

	@RequestMapping(value = "/redis/reshard")
	@ResponseBody
	public void reshard(@RequestBody RedisReshardParams redisReshardParams) {

		ReshardPlan reshardPlan = reshardService.createReshardPlan(redisReshardParams.getCluster(), redisReshardParams.getSrcNodes(),
				redisReshardParams.getDesNodes(), redisReshardParams.isAverage());
		RedisReshardTask task = new RedisReshardTask(reshardPlan);
		TaskManager.submit(task);
	}

	@RequestMapping(value = "/redis/failover")
	@ResponseBody
	public boolean failover(@RequestBody RedisScaleParams redisScaleParams){
		return RedisManager.failover(redisScaleParams.getCluster(),redisScaleParams.getSlaveAddress());
	}

	@RequestMapping(value = "/redis/addslave")
	@ResponseBody
	public void addSlave(@RequestBody RedisScaleParams redisScaleParams){
		RedisScaler.addSlave(redisScaleParams.getCluster(),redisScaleParams.getMasterAddress());
	}


	@RequestMapping(value = "/redis/deleteslave")
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
