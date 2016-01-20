package com.dianping.cache.service;

import com.dianping.cache.entity.CategoryStats;

import java.util.List;

/**
 * Created by thunder on 16/1/18.
 */
public interface RdbService {

    List<CategoryStats> getMergeStat(int days);

}
