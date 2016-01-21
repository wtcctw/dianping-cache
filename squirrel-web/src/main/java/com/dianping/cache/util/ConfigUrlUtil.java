package com.dianping.cache.util;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/19.
 */
public class ConfigUrlUtil {
    private static String REDIS_URL_PREFIX = "redis-cluster://";

    private static CacheConfigurationService cacheConfigurationService;

    static {
        cacheConfigurationService = SpringLocator.getBean("cacheConfigurationService");
    }

    public static List<String> serverList(CacheConfiguration configuration){
        checkNotNull(configuration,"configuration is null");
        return serverList(configuration.getServers());
    }
    public static List<String> serverList(String servers){
        checkNotNull(servers, "servers is null");
        if(servers.startsWith(REDIS_URL_PREFIX)){
            String addressPart = servers.substring(REDIS_URL_PREFIX.length(), servers.indexOf('?'));
            List<String> serverList = new ArrayList<String>();
            String[] array = addressPart.split(",");
            for (String s : array) {
                serverList.add(s);
            }
            return serverList;
        }
        return Collections.emptyList();
    }

    public static Map<String,String> properties(CacheConfiguration configuration){
        checkNotNull(configuration,"configuration is null");
        return properties(configuration.getServers());
    }
    public static Map<String,String> properties(String servers){
        checkNotNull(servers, "servers is null");

        if (servers.startsWith(REDIS_URL_PREFIX)) {
            Map<String,String> properties = new HashMap<String, String>();
            String infoStr = servers.substring(servers.indexOf('?')+1);
            String[] infoArray = infoStr.split("&");
            for(String item : infoArray){
                properties.put(item.split("=")[0],item.split("=")[1]);
            }
            return properties;
        }
        return Collections.emptyMap();
    }


    public static String getProperty(CacheConfiguration configuration,String propertyKey) {
        return  properties(configuration).get(propertyKey);
    }

    public static String getProperty(String servers,String propertyKey) {
        return  properties(servers).get(propertyKey);
    }

    public static String spliceRedisUrl(List<String> serverList,Map<String,String> properties){
        checkNotNull(serverList, "serverList is null");
        checkNotNull(properties, "properties is null");
        String addressPart = StringUtils.join(serverList,",");
        String[] propertiesArray = new String[properties.size()];
        int index = 0;
        for(Map.Entry<String,String> entry : properties.entrySet()){
            if(StringUtils.isNotBlank(entry.getValue())){
                propertiesArray[index++] = entry.getKey()+"="+entry.getValue();
            }
        }
        String propertiesPart = StringUtils.join(propertiesArray,"&");
        return REDIS_URL_PREFIX+addressPart+"?"+propertiesPart;
    }

    public static String getPassword(String cluster){
        CacheConfiguration configuration = cacheConfigurationService.find(cluster);
        return getProperty(configuration,"password");
    }


}
