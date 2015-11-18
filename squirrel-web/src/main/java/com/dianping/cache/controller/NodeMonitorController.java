package com.dianping.cache.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerClusterService;
import com.dianping.cache.service.ServerService;
@Controller
public class NodeMonitorController  extends AbstractSidebarController{

	@Resource(name = "cacheConfigurationService")
	private CacheConfigurationService cacheConfigurationService ;
	
	@Resource(name = "serverClusterService")
	private ServerClusterService serverClusterService;
	
	@Resource(name = "memcacheStatsService")
	private MemcacheStatsService memcacheStatsService;
	
	@Resource(name = "serverService")
	private ServerService serverService;
	
	@RequestMapping(value = "/monitor/node", method = RequestMethod.GET)
	public ModelAndView viewCacheConfig(HttpServletRequest request, HttpServletResponse response){
		
		return new ModelAndView("monitor/nodedashboard",createViewMap());
	}
	@Override
	protected String getSide() {
		// TODO Auto-generated method stub
		return "node";
	}

	@Override
	public String getSubSide() {
		// TODO Auto-generated method stub
		return "node";
	}

	@Override
	protected String getMenu() {
		// TODO Auto-generated method stub
		return "monitor";
	}

}
