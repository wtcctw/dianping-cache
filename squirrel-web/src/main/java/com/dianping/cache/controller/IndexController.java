package com.dianping.cache.controller;

import com.dianping.cache.alarm.entity.ScanStatistics;
import com.dianping.cache.alarm.report.scanService.ScanStatisticsService;
import com.dianping.cache.alarm.utils.DateUtil;
import com.dianping.cache.controller.vo.IndexData;
import com.dianping.cache.controller.vo.MemcachedDashBoardData;
import com.dianping.cache.controller.vo.RedisDashBoardData;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.scale.instance.docker.paasbean.MachineStatusBean;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.PaasApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController extends AbstractMenuController{
	private static final long MS_PER_HOUR = 1000 * 60 * 60;
	private static final long MS_PER_DAY = MS_PER_HOUR * 24;

	@Autowired
	private CacheConfigurationService cacheConfigurationService;

	@Autowired
	private MemcachedController memcachedController;

	@Autowired
	private RedisController redisController;

	@Autowired
	private PaasApiService paasApiService;


	@Autowired
	private ScanStatisticsService scanStatisticsService;

	@RequestMapping(value = "/")
	public ModelAndView allApps() {
		return new ModelAndView("cluster/dashboard",createViewMap());
	}

	@RequestMapping(value = "/dashdata")
	@ResponseBody
	public IndexData dashdata(){
		IndexData data = new IndexData();

		getMemcacheRedisIndexData(data);

		getMachineStatusIndexData(data);

		getScanStatistics(data);

		return data;
	}


	private void getMemcacheRedisIndexData(IndexData data){
		long start = (System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS))/1000;
		int total = 0;
		int redisCount = 0,memcachedCount = 0,redisInc = 0,memcachedInc=0;
		List<CacheConfiguration> allCluster = cacheConfigurationService.findAll();
		total = allCluster.size();
		for(CacheConfiguration configuration : allCluster){
			if(configuration.getCacheKey().contains("redis")){
				redisCount++;
				if(configuration.getAddTime() > start){
					redisInc++;
				}
			}else if(configuration.getCacheKey().contains("memcached")){
				memcachedCount++;
				if(configuration.getAddTime() > start){
					memcachedInc++;
				}
			}
		}

		RedisDashBoardData redisDashBoardData = redisController.getRedisDashboard();
		MemcachedDashBoardData memcachedDashBoardData = memcachedController.getDashboardData();

		int redisCapacity = 0,memcachedCapacity = 0;
		for(RedisDashBoardData.SimpleAnalysisData rs : redisDashBoardData.getDatas()){
			redisCapacity += rs.getMaxMemory() * 2;
		}
		for(MemcachedDashBoardData.SimpleAnalysisData ms : memcachedDashBoardData.getDatas()){
			memcachedCapacity += ms.getMaxMemory();
		}

		data.setTotalNum(total);
		data.setCountInc(redisInc+memcachedInc);
		data.setRedisCapacity(redisCapacity);
		data.setMemcachedCapacity(memcachedCapacity);
		data.setCapacity(redisCapacity + memcachedCapacity);
	}

	private void getMachineStatusIndexData(IndexData data) {

		List<MachineStatusBean> beans = paasApiService.getMachineStatus();
		long totalMC = 0,freeMC = 0;
		for(MachineStatusBean bean : beans){
			totalMC += bean.getMemory();
			freeMC += bean.getMemoryFree();
			//System.out.println(bean.getIp() + "/" + bean.getMemoryFree());
		}
		data.setTotalMachines(beans.size());
		data.setTotalMachineCapacity((int) (totalMC / 1024 / 1024 / 1024 / 2));
		data.setFreeMachineCapacity((int) (freeMC / 1024 / 1024 / 1024 / 2));

	}

	private void getScanStatistics(IndexData data) {

		List<ScanStatistics> scanStatisticsList = new ArrayList<ScanStatistics>();
		Date now = new Date();
		for(int i = 1;i <= 7;i ++){
			Date day = new Date(now.getTime() - i * MS_PER_DAY);
			String dayText =   DateUtil.getCatDayString(day);
			List<ScanStatistics> statistics = scanStatisticsService.findByCreateTime(dayText);
			if(0 != statistics.size()) {
				scanStatisticsList.add(statistics.get(0));
			}
		}

		List<String> createTimeList = new ArrayList<String>();
		List<Long> totalCountListSquirrel = new ArrayList<Long>();
		List<Double> failurePercentListSquirrel = new ArrayList<Double>();
		List<Double> avgDelayListSquirrel = new ArrayList<Double>();
		List<Long> totalCountListCache = new ArrayList<Long>();
		List<Double> failurePercentListCache = new ArrayList<Double>();
		List<Double> avgDelayListCache = new ArrayList<Double>();

		DecimalFormat df   = new DecimalFormat("######0.00");
		Collections.reverse(scanStatisticsList);
		for(ScanStatistics statistics: scanStatisticsList){
			createTimeList.add(statistics.getCreateTime());
			totalCountListSquirrel.add(statistics.getTotalCountSquirrel() / 100000000);
			totalCountListCache.add(statistics.getTotalCountCache() / 100000000);
			failurePercentListSquirrel.add(statistics.getFailurePercentSquirrel());
			failurePercentListCache.add(statistics.getFailurePercentCache());
			avgDelayListSquirrel.add(Double.parseDouble(df.format(statistics.getAvgDelaySquirrel())));
			avgDelayListCache.add(Double.parseDouble(df.format(statistics.getAvgDelayCache())));

		}

		data.setCreateTimeList(createTimeList);
		data.setTotalCountListSquirrel(totalCountListSquirrel);
		data.setTotalCountListCache(totalCountListCache);
		data.setFailurePercentListSquirrel(failurePercentListSquirrel);
		data.setFailurePercentListCache(failurePercentListCache);
		data.setAvgDelayListSquirrel(avgDelayListSquirrel);
		data.setAvgDelayListCache(avgDelayListCache);
	}

}