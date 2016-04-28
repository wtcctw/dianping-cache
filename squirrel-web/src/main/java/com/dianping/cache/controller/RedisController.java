package com.dianping.cache.controller;

import com.dianping.cache.controller.vo.*;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.RedisStats;
import com.dianping.cache.scale.ScaleException;
import com.dianping.cache.scale.cluster.redis.*;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.cache.service.RedisService;
import com.dianping.cache.service.ReshardService;
import com.dianping.cache.util.NetUtil;
import com.dianping.squirrel.service.AuthService;
import com.dianping.squirrel.task.RedisReshardTask;
import com.dianping.squirrel.task.TaskManager;
import com.dianping.squirrel.view.highcharts.ChartsBuilder;
import com.dianping.squirrel.view.highcharts.HighChartsWrapper;
import com.dianping.squirrel.view.highcharts.statsdata.RedisClusterData;
import com.dianping.squirrel.view.highcharts.statsdata.RedisStatsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


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

	@Autowired
	private AuthService authService;

	private String subside;

//	@RequestMapping(value = "/redis/dashboard")
//	public ModelAndView viewClusterDashBoard(){
//		return new ModelAndView("monitor/redisdashboard",createViewMap());
//	}
//
//	/**
//	 * @deprecated
//	 * @return
//     */
//	@RequestMapping(value = "/redis/dashboardinfo")
//	@ResponseBody
//	public List<RedisClusterData> getDashboard(){
//		return RedisDataUtil.getClusterData();
//	}

	@RequestMapping(value = "/redis/data/dashboard")
	@ResponseBody
	public RedisDashBoardData getRedisDashboard(){
		return new RedisDashBoardData(RedisManager.getClusterCache().values());
	}
	@RequestMapping(value = "/redis/data/history")
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistory(String cluster,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(100, TimeUnit.DAYS))/1000;
		long end = endTime/1000;
		List<RedisStats> data = redisService.findByServerWithInterval(cluster, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}




	/**
	 * @deprecated
	 * @return
	 */
	@RequestMapping(value = "/redis/serverinfo")
	public ModelAndView viewRedisServerInfo(){
		return new ModelAndView("monitor/redisserverinfo",createViewMap());
	}


	@RequestMapping(value = "/redis")
	public ModelAndView redis(){
		subside = "redis";
		return new ModelAndView("cluster/redis",createViewMap());
	}

	@RequestMapping(value = "/redis/{cluster}")
	public ModelAndView getRedisDetail(@PathVariable("cluster") String cluster){
		subside = "redis";
		return new ModelAndView("cluster/redisdetail",createViewMap());
	}

	@RequestMapping(value = "/redis/{cluster}/edit")
	public ModelAndView edit(@PathVariable("cluster") String cluster){
		subside = "redis";
		return new ModelAndView("cluster/edit",createViewMap());
	}

	@RequestMapping(value = "/redis/{cluster}/modifypassword")
	public boolean password(){
		//authService.
		return true;
	}

	@RequestMapping(value = "/redis/{cluster}/authorize")
	public boolean authorize(@PathVariable(value = "cluster") String cluster,@RequestParam String application) throws Exception {
		authService.authorize(application,cluster);
		return true;
	}

	@RequestMapping(value = "/redis/editdata")
	@ResponseBody
	public CacheConfiguration editRedis(@RequestParam String cluster,@RequestParam String swimlane){
		CacheConfiguration configuration = cacheConfigurationService.findWithSwimLane(cluster,swimlane);
		return configuration;
	}

	@RequestMapping(value = "/redis/{cluster}/detail")
	@ResponseBody
	public RedisDashBoardData.SimpleAnalysisData getRedisDetailData(@PathVariable(value = "cluster") String cluster){
		RedisCluster redisCluster =  RedisManager.getRedisCluster(cluster);
		RedisDashBoardData data = new RedisDashBoardData();
		RedisDashBoardData.SimpleAnalysisData simpleAnalysisData = data.new SimpleAnalysisData(redisCluster);
		simpleAnalysisData.analysis();
		return simpleAnalysisData;
	}


	@RequestMapping(value = "/redis/data/applications")
	@ResponseBody
	public List<String> getApplications(String cluster) throws Exception {
		return authService.getAuthorizedApplications(cluster);
	}

	@RequestMapping(value = "/redis/data/auth")
	@ResponseBody
	public void getAuth(String cluster) throws Exception {
		// authService.getAuth(cluster);
	}

	@RequestMapping(value = "/redis/historydata")
	@ResponseBody
	public List<HighChartsWrapper> getRedisHistoryData(String address,long endTime){
		long start = (endTime - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))/1000;
		long end = endTime/1000;
		List<RedisStats> data = redisService.findByServerWithInterval(address, start, end);
		RedisStatsData statsData = new RedisStatsData(data);
		return ChartsBuilder.buildRedisStatsCharts(statsData);
	}

	@RequestMapping(value = "/redis/period")
	@ResponseBody
	public List<HighChartsWrapper> period(String address,long endTime,int period){

		List<RedisStats> periodStats =  redisService.findPeriodicStats(address,endTime/1000,1,30);
		final HighChartsWrapper chartsWrapper = ChartsBuilder.buildPeriodCharts(periodStats,1,endTime,30);
		return new ArrayList<HighChartsWrapper>(){{
			add(chartsWrapper);
		}};
	}

	@RequestMapping(value = "/redis/{cluster}/period")
	@ResponseBody
	public HighChartsWrapper clusterperiod(@PathVariable(value = "cluster")String cluster){
		RedisCluster redisCluster = RedisManager.getRedisCluster(cluster);
		List<RedisNode> nodes = redisCluster.getNodes();
		long endTime = System.currentTimeMillis();
		List<RedisStats> clusterRedisStats = new ArrayList<RedisStats>(31);
		for(RedisNode node : nodes){
			List<RedisStats> periodStats =  redisService.findPeriodicStats(node.getMaster().getAddress(),endTime/1000,1,30);
			for(int index = 0; index < 31; index++){
				if(clusterRedisStats.size() < index + 1){
					clusterRedisStats.add(new RedisStats());
				}
				if(periodStats.get(index) != null){
					long used = clusterRedisStats.get(index).getMemory_used();
					clusterRedisStats.get(index).setMemory_used(used+periodStats.get(index).getMemory_used());
				}
			}
		}
		HighChartsWrapper chartsWrapper = ChartsBuilder.buildPeriodCharts(clusterRedisStats,1,endTime,30);
		return chartsWrapper;
	}

	@RequestMapping(value = "/redis/auth/setPassword")
	@ResponseBody
	public void setPassword(@RequestBody AuthParams authParams){

	}




	@RequestMapping(value = "/redis/reshard")
	@ResponseBody
	public String reshard(@RequestBody RedisReshardParams redisReshardParams) {

		ReshardPlan reshardPlan = reshardService.createReshardPlan(redisReshardParams);
		RedisReshardTask task = new RedisReshardTask(reshardPlan);

		TaskManager.submit(task);
		return NetUtil.getFirstLocalIp();
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

	@RequestMapping(value = "/redis/new")
	@ResponseBody
	public boolean newCluster(@RequestBody NewClusterParams newClusterParams){
		//RedisScaler.addSlave(redisScaleParams.getCluster(),redisScaleParams.getMasterAddress());
		return false;
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
