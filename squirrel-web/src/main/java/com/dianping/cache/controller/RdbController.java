package com.dianping.cache.controller;

import com.dianping.cache.dao.CategoryStatsDao;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thunder on 16/1/14.
 */

@Controller
public class RdbController extends AbstractSidebarController{

    @Resource(name = "categoryStatsDao")
    CategoryStatsDao categoryStatsDao;

    @RequestMapping(value = "/rdb/stat")
    public ModelAndView viewServers() {
        return new ModelAndView("rdb/stat");
    }

    @RequestMapping(value = "/rdb/data")
    @ResponseBody
    public Object getCategoryData() {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("data", categoryStatsDao.selectAll());
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
