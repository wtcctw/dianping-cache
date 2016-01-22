package com.dianping.cache.controller;

import com.dianping.cache.controller.vo.CategoryWrapperData;
import com.dianping.cache.controller.vo.ConfigurationParams;
import com.dianping.cache.entity.CacheConfiguration;
import com.dianping.cache.entity.CacheKeyConfiguration;
import com.dianping.cache.service.CacheConfigurationService;
import com.dianping.cache.service.CacheKeyConfigurationService;
import com.dianping.cache.service.RdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dp on 16/1/11.
 */
@Controller
public class ConfigController extends AbstractSidebarController {

    @Autowired
    private CacheKeyConfigurationService cacheKeyConfigurationService;

    @Autowired
    private CacheConfigurationService cacheConfigurationService;

    @Autowired
    private RdbService rdbService;

    private String subside;

    @RequestMapping(value = "/config/cluster")
    public ModelAndView clusterConfig() {
        subside = "clusterconfig";
        return new ModelAndView("cache/config", createViewMap());
    }

    @RequestMapping(value = "/config/category")
    public ModelAndView categoryConfig() {
        subside = "categoryconfig";
        return new ModelAndView("cache/key", createViewMap());
    }

    @RequestMapping(value = "/config/cluster/edit")
    public ModelAndView editConfig() {

        return new ModelAndView("cache/configedit");
    }

    @RequestMapping(value = "/config/configuration/1")
    @ResponseBody
    public CacheConfiguration getConfig(@RequestBody ConfigurationParams configurationParams) {
        return cacheConfigurationService.findWithSwimLane(configurationParams.getCacheKey(), configurationParams.getSwimlane());
    }

    @RequestMapping(value = "/config/cluster/findAll")
    @ResponseBody
    public List<CacheConfiguration> getConfigurations() {
        return cacheConfigurationService.findAll();
    }

    @RequestMapping(value = "/config/category/findbycluster")
    @ResponseBody
    public List<CacheKeyConfiguration> getCategoryByCluster(@RequestParam String cluster) {
        return cacheKeyConfigurationService.findByCacheType(cluster);
    }

    @RequestMapping(value = "/config/category/findbycluster/rdb")
    @ResponseBody
    public List<CategoryWrapperData> getCategoryWithRDB(@RequestParam String cluster) {
        List<CacheKeyConfiguration> categorylist = cacheKeyConfigurationService.findByCacheType(cluster);
        List<CategoryWrapperData> result = new ArrayList<CategoryWrapperData>(categorylist.size());
        for(CacheKeyConfiguration category : categorylist){
            CategoryWrapperData data = new CategoryWrapperData();
            data.setCategory(category);
            RdbService.TotalStat stat = rdbService.getCategoryMergeStat(category.getCategory());
            data.setCount(stat.count);
            data.setSize(stat.volumn/1024/1024);
            result.add(data);
        }
        return result;
    }


    @RequestMapping(value = "/config/cluster/update")
    @ResponseBody
    public void update() {

    }

    @Override
    protected String getSide() {
        return "config";
    }

    @Override
    public String getSubSide() {
        return subside;
    }
}
