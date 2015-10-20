package com.dianping.cache.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController extends AbstractMenuController{

	private static final Logger logger = LoggerFactory
			.getLogger(IndexController.class);
	
	@RequestMapping(value = "/")
	public ModelAndView allApps(HttpServletRequest request,
			HttpServletResponse response) { 
		return new ModelAndView("cache/config",createViewMap());
	}
	
	
	@Override
	protected String getMenu() {
		return "index";
	}
	
}
