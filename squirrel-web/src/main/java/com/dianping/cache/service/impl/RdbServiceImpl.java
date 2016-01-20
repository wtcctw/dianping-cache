package com.dianping.cache.service.impl;

import com.dianping.cache.dao.CategoryStatsDao;
import com.dianping.cache.entity.CategoryStats;
import com.dianping.cache.service.RdbService;
import com.dianping.cache.util.CommonUtil;

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
            HashMap<String, Object> para = new HashMap<String, Object>();
            para.put("start", start);
            para.put("end", end);
            List<CategoryStats> list = categoryStatsDao.selectAllByTime(para);
            for(CategoryStats c : list) {
                c.setKeySizeRead(CommonUtil.ConvertBytesName(c.getKeySize()));
                c.setValueSizeRead(CommonUtil.ConvertBytesName(c.getValueSize()));
            }
            List<CategoryStats> dayResult = mergeByCategory(list);
            result.addAll(dayResult);
        }
        return result;
    }


    @Override
    public TotalStat getCategoryMergeStat(String category) {
        List<CategoryStats> data = categoryStatsDao.selectCategoryStat(category);
        List<CategoryStats> result = mergeByCategory(data);
        TotalStat totalStat = new TotalStat();
        for(CategoryStats cs : result) {
            totalStat.count += cs.getKeyCount();
            totalStat.volumn += cs.getKeySize();
            totalStat.volumn += cs.getValueSize();
        }
        return totalStat;
    }

    List<CategoryStats> filterOldData(List<CategoryStats> data) {
        Map<String, CategoryStats> map = new HashMap<String, CategoryStats>();
        for(CategoryStats c : data) {
            String category = c.getCategory();
            if(map.get(category) == null) {
                map.put(c.getCategory() + c.getHostAndPort(), c);
            } else {
                CategoryStats old = map.get(c.getCategory() + c.getHostAndPort());
                if(old.getUpdateTime() < c.getUpdateTime())
                    map.put(c.getCategory() + c.getHostAndPort(), c);
            }
        }
        List<CategoryStats> result = new ArrayList<CategoryStats>();
        result.addAll(map.values());
        return result;
    }

    List<CategoryStats> mergeByCategory(List<CategoryStats> data) {
        Map<String, CategoryStats> map = new HashMap<String, CategoryStats>();
        data = filterOldData(data);
        for(CategoryStats c : data) {
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
