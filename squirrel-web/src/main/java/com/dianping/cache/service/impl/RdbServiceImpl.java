package com.dianping.cache.service.impl;

import com.dianping.cache.dao.CategoryStatsDao;
import com.dianping.cache.entity.CategoryStats;
import com.dianping.cache.service.RdbService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thunder on 16/1/18.
 */
public class RdbServiceImpl implements RdbService{

    @Resource(name = "categoryStatsDao")
    CategoryStatsDao categoryStatsDao;

    @Override
    public List<CategoryStats> getMergeStat(int days) {
        final long DAYMILL = 3600 * 24 * 1000 * 1000;
        List<CategoryStats> result = new ArrayList<CategoryStats>();
        for(int i = 0; i <= days; i++) {
            long currentMill = System.currentTimeMillis();
            long end = currentMill - i * DAYMILL ;
            long start = currentMill - (i + 1) * DAYMILL;
            List<CategoryStats> list = categoryStatsDao.selectAllByTime(start, end);
            List<CategoryStats> dayResult = mergeByCategory(list);
            result.addAll(dayResult);
        }
        return result;
    }

    List<CategoryStats> mergeByCategory(List<CategoryStats> list) {
        Map<String, CategoryStats> map = new HashMap<String, CategoryStats>();
        for(CategoryStats c : list) {
            if(map.get(c.getCategory()) == null) {
                map.put(c.getCategory(), c);
            } else {
                CategoryStats statInMap = map.get(c.getCategory());
                statInMap.setKeyCount(statInMap.getKeyCount() + c.getKeyCount());
                statInMap.setValueSize(statInMap.getValueSize() + c.getValueSize());
                statInMap.setKeySize(statInMap.getKeySize() + c.getKeySize());
            }
        }
        List<CategoryStats> result = new ArrayList<CategoryStats>();
        for(CategoryStats c : map.values()) {
            result.add(c);
        }
        return result;
    }
}
