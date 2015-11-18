package com.dianping.cache.controller;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMenuController extends AbstractController {
	public static final String MENU_KEY = "menu";
	public static final String CONTENT_PATH_KEY = "contextPath";
	
	protected abstract String getMenu();
	
	protected Map<String, Object> createViewMap() {
		
		Map<String, Object> paras = new HashMap<String, Object>();
		
		paras.put(MENU_KEY, getMenu());
		paras.put(CONTENT_PATH_KEY, "");
		return paras;
	}

}
