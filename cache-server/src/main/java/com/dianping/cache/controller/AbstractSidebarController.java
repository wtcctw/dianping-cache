package com.dianping.cache.controller;

import java.util.Map;

public abstract class AbstractSidebarController extends AbstractMenuController {

	private String SIDE_KEY    =  "side"; 

	private String SUB_SIDE_KEY =  "subside";

	protected abstract String getSide();
	
	public abstract String getSubSide();
	

	protected Map<String, Object> createViewMap() {
		
		Map<String, Object> paras = super.createViewMap();
		
		paras.put(SIDE_KEY, getSide());
		paras.put(SUB_SIDE_KEY, getSubSide());
		
		return paras;
	}


}
