package com.dianping.cache.dao;

import com.dianping.cache.entity.CategoryStats;

import java.util.List;

/**
 * Created by thunder on 16/1/14.
 */
public interface CategoryStatsDao {

    List<CategoryStats> selectAll();

    List<CategoryStats> selectAllByTime(long start, long end);

}
