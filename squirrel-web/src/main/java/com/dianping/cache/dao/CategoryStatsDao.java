package com.dianping.cache.dao;

import com.dianping.cache.entity.CategoryStats;

import java.util.List;
import java.util.Map;

/**
 * Created by thunder on 16/1/14.
 */
public interface CategoryStatsDao {

    List<CategoryStats> selectAll();

    List<CategoryStats> selectAllByTime(Map para);

    List<CategoryStats> selectCategoryStat(String category);

}
