package com.dianping.cache.controller;

import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.MemcacheStats;
import com.dianping.cache.entity.Server;
import com.dianping.cache.monitor.statsdata.MemcachedStatsData;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.MemcacheStatsService;
import com.dianping.cache.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dp on 16/1/4.
 */
@Controller
public class MemcachedController extends AbstractCacheController{

    @Autowired
    private CacheConfigurationService cacheConfigurationService ;
    @Autowired
    private ServerService serverService;
    @Autowired
    private MemcacheStatsService memcacheStatsService;

    @RequestMapping(value = "/memcached")
    public ModelAndView viewCacheConfig() {
        subside = "memcached";
        return new ModelAndView("cluster/memcached", createViewMap());
    }

    @RequestMapping(value = "/memcached/dashboard/data")
    @ResponseBody
    public void getDashboardData(){
        List<CacheConfiguration> configList = cacheConfigurationService.findAll();
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
        Map<String, Map<String, Object>> currentServerStats = this.getCurrentServerStatsData();
    }

    private  Map<String,Map<String,Object>> getCurrentServerStatsData(){
        Map<String, List<MemcacheStats>> serverStats = getAllServerStats();
        Map<String,MemcachedStatsData> serverStatsData = convertStats(serverStats);
        Map<String,Map<String,Object>> currentStats = new HashMap<String,Map<String,Object>>();
        for(Map.Entry<String,MemcachedStatsData> item : serverStatsData.entrySet()){
            Map<String,Object> temp = new HashMap<String,Object>();
            if(item.getValue() == null){
                continue;
            }
            int length = item.getValue().getLength();
            temp.put("QPS", item.getValue().getHitDatas()[length-1]);
            temp.put("max_memory", item.getValue().getMax_memory());
            temp.put("used_memory", item.getValue().getBytes()[length-1]);
            temp.put("curr_conn", item.getValue().getConnDatas()[length-1]);
            temp.put("evict", item.getValue().getEvictionsDatas()[length-1]);
            long miss = item.getValue().getGetMissDatas()[length-1];
            long get = item.getValue().getGetsDatas()[length-1];
            float hitrate;
            if(get > 0){
                hitrate  = (float) ((double)get/(miss+get));
            }
            else{
                hitrate = 1.0f;
            }
            temp.put("hitrate", hitrate);
            currentStats.put(item.getKey(), temp);
        }
        return currentStats;
    }

    private  Map<String,List<MemcacheStats>> getAllServerStats() {
        //get all server in cluster
        List<Server> sc = serverService.findAllMemcachedServers();

        long start = (System.currentTimeMillis()- TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES))/1000;
        long end = (System.currentTimeMillis())/1000;

        Map<String,List<MemcacheStats>> result = new HashMap<String,List<MemcacheStats>>();
        for(Server server : sc){
            result.put(server.getAddress(),getMemcacheStats(server.getAddress(),start,end));
        }
        return result;
    }

    private List<MemcacheStats> getMemcacheStats(String address,long start,long end){
        List<MemcacheStats> result = memcacheStatsService.findByServerWithInterval(address, start, end);
        return result;
    }

    private Map<String, MemcachedStatsData> convertStats(
            Map<String, List<MemcacheStats>> stats) {

        Map<String,MemcachedStatsData> result = new HashMap<String,MemcachedStatsData>();
        for(Map.Entry<String, List<MemcacheStats>> item : stats.entrySet()){
            if(item.getValue() != null && item.getValue().size() > 1){
                result.put(item.getKey(), new MemcachedStatsData(item.getValue()));
            }
//			else{
//				result.put(item.getKey(),null);
//			}
        }
        return result;
    }
    private String subside;

    @Override
    protected String getSide() {
        return "cluster";
    }

    @Override
    public String getSubSide() {
        return null;
    }
}
