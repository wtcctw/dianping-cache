package com.dianping.cache.util;

import java.util.ArrayList;
import java.util.List;

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
}
