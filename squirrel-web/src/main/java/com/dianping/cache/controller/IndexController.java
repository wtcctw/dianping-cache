package com.dianping.cache.controller;



import com.dianping.cache.controller.vo.IndexData;
import com.dianping.cache.controller.vo.MemcachedDashBoardData;
import com.dianping.cache.controller.vo.RedisDashBoardData;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Controller
public class IndexController extends AbstractMenuController{

	@Autowired
	private CacheConfigurationService cacheConfigurationService;

	@Autowired
	private MemcachedController memcachedController;

	@Autowired
	private RedisController redisController;

	@RequestMapping(value = "/")
	public ModelAndView allApps() {
		return new ModelAndView("cluster/dashboard",createViewMap());
	}

	@RequestMapping(value = "/dashdata")
	@ResponseBody
	public IndexData dashdata(){
		IndexData data = new IndexData();
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
		return data;
	}
}
