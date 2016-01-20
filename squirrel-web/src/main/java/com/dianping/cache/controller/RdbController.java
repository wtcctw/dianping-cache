package com.dianping.cache.controller;

import com.dianping.cache.dao.CategoryStatsDao;
import com.dianping.cache.entity.CategoryStats;
import com.dianping.cache.service.RdbService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thunder on 16/1/14.
 */

@Controller
public class RdbController extends AbstractSidebarController{

    @Resource(name = "categoryStatsDao")
    CategoryStatsDao categoryStatsDao;

    @Resource(name = "RdbService")
    RdbService rdbService;

    @RequestMapping(value = "/rdb/stat")
    public ModelAndView viewServers() {
        return new ModelAndView("rdb/stat");
    }

    @RequestMapping(value = "/rdb/dayData")
    @ResponseBody
    public Object getCategoryDayData(@RequestParam("day")int day) {
        List<CategoryStats> data = rdbService.getMergeStat(day);
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("data", data);
        return para;
    }



    @RequestMapping(value = "/rdb/data")
    @ResponseBody
    public Object getCategoryData() {
        Map<String, Object> para = new HashMap<String, Object>();
        List<CategoryStats> data = categoryStatsDao.selectAll();
        para.put("data", data);
        return para;
    }
    @Override
    protected String getSide() {
        return null;
    }

    @Override
    public String getSubSide() {
        return null;
    }

}
