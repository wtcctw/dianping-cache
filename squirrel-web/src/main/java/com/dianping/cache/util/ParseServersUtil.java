package com.dianping.cache.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseServersUtil {
	public static List<String> parseRedisServers(String url){
		String URL_PREFIX = "redis-cluster://";
		if(url == null || !url.startsWith(URL_PREFIX)) {
	            return null;
	    }
		String servers = url.substring(URL_PREFIX.length(), url.indexOf('?'));
		
		List<String> serverList = new ArrayList<String>();
		String[] array = servers.split(",");
		for (String s : array) {
			serverList.add(s);
		}
		return serverList;
	}

	public static Map<String,String> parseRedisUrlInfo(String url){
		String URL_PREFIX = "redis-cluster://";
		if(url == null || !url.startsWith(URL_PREFIX)) {
			return null;
		}
		Map<String,String> info = new HashMap<String, String>();
		String infoStr = url.substring(url.indexOf('?')+1);
		String[] infoArray = infoStr.split("&");
		for(String item : infoArray){
			info.put(item.split("=")[0],item.split("=")[1]);
		}
		return info;
	}

}
