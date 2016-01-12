package com.dianping.cache.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.cache.monitor.NotifyManager;


@Controller
public class IndexController extends AbstractMenuController{

	private static final Logger logger = LoggerFactory
			.getLogger(IndexController.class);
	
	@RequestMapping(value = "/")
	public ModelAndView allApps(HttpServletRequest request,
			HttpServletResponse response) { 
		return new ModelAndView("cache/config",createViewMap());
	}
	
	@RequestMapping(value = "/send")
	public void sendsms(HttpServletRequest request,
			HttpServletResponse response) { 
		NotifyManager.getInstance().notifySms2("Justtest");
	}
	
	@Override
	protected String getMenu() {
		return "index";
	}
	
}
