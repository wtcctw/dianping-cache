package com.dianping.cache.controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController extends AbstractMenuController{

	@RequestMapping(value = "/")
	public ModelAndView allApps() {
		return new ModelAndView("cache/config",createViewMap());
	}
	
	@Override
	protected String getMenu() {
		return "index";
	}
	
}
