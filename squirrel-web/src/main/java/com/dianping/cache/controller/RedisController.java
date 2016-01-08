package com.dianping.cache.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;


import com.dianping.cache.controller.dto.RedisDashBoardData;
import com.dianping.cache.controller.dto.RedisReshardParams;
import com.dianping.cache.controller.dto.RedisScaleParams;
import com.dianping.cache.deamontask.CacheDeamonTaskManager;
import com.dianping.cache.deamontask.tasks.RedisReshardTask;
import com.dianping.cache.scale.cluster.redis.RedisManager;
import com.dianping.cache.scale.cluster.redis.RedisScaler;
import com.dianping.cache.scale.cluster.redis.ReshardPlan;
import com.dianping.cache.scale.exceptions.ScaleException;
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
import com.dianping.cache.service.RedisStatsService;


@Controller
public class RedisController extends AbstractCacheController{


	@Autowired
	private RedisStatsService redisStatsService;

	@Autowired
	private ReshardService reshardService;

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
	@RequestMapping(value = "/redis/detail")
	@ResponseBody
	public Map<String, Object> getRedisDetailData(){
		return RedisDataUtil.getRedisDetailData(currentCluster);
	}

	@RequestMapping(value = "/redis/historydata")
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistoryData(String address,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(120, TimeUnit.MINUTES))/1000;
		long end = endTime/1000;
		List<RedisStats> data = redisStatsService.findByServerWithInterval(address, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}

	@RequestMapping(value = "/redis/reshard")
	@ResponseBody
	public void reshard(@RequestBody RedisReshardParams redisReshardParams) {

		ReshardPlan reshardPlan = reshardService.createReshardPlan(redisReshardParams.getCluster(), redisReshardParams.getSrcNodes(),
				redisReshardParams.getDesNodes(), redisReshardParams.isAverage());
		RedisReshardTask task = new RedisReshardTask(reshardPlan);
		CacheDeamonTaskManager.submit(task);
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
