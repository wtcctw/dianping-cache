package com.dianping.cache.controller;

import com.dianping.cache.dao.CategoryStatsDao;
import com.dianping.cache.entity.CategoryStats;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping(value = "/rdb/stat")
    public ModelAndView viewServers() {
        return new ModelAndView("rdb/stat");
    }

    @RequestMapping(value = "/rdb/data")
    @ResponseBody
    public Object getCategoryData() {
        Map<String, Object> para = new HashMap<String, Object>();
        List<CategoryStats> data = categoryStatsDao.selectAll();
//        long cur = System.currentTimeMillis();
//        List<CategoryStats> list = new ArrayList<CategoryStats>();//List<CategoryStats>();
//        for(int day = 1; day <= 7; day++) {
//            Map<String, CategoryStats> categoryToStats = new HashMap<String, CategoryStats>();
//            for(int i = 0; i < data.size(); i++) {
//                String category = data.get(i).getCategory();
//                CategoryStats curStat = data.get(i);
//                if(curStat.getUpdateTime() - cur > 3600 * 24 * 1000 * (day + 1)
//                        || curStat.getUpdateTime() - cur < 3600 * 24 * 1000 * (day - 1))
//                    continue;
//                if(categoryToStats.get(category) == null)
//                    categoryToStats.put(category, data.get(i));
//                else {
//                    CategoryStats original = categoryToStats.get(category);
//                    original.setKeyCount(curStat.getKeyCount() + original.getKeyCount());
//                    original.setKeySize(curStat.getKeySize() + original.getKeySize());
//                    original.setValueSize(curStat.getValueSize() + original.getValueSize());
//                }
//            }
//            List<CategoryStats> dayData = new ArrayList<CategoryStats>();
//            for(Map.Entry<String, CategoryStats> cs : categoryToStats.entrySet()) {
//                CategoryStats stat = cs.getValue();
//                dayData.add(stat);
//            }
//            for(CategoryStats categoryStats : dayData)
//                list.add(categoryStats);
//        }
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
