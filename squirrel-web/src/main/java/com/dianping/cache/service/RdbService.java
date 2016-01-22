package com.dianping.cache.service;

import com.dianping.cache.entity.CategoryStats;
import com.dianping.cache.service.impl.RdbServiceImpl;

import java.util.List;

/**
 * Created by thunder on 16/1/18.
 */
public interface RdbService {

    List<CategoryStats> getMergeStat(int days);

    TotalStat getCategoryMergeStat(String category);

    class TotalStat {
        public long count = 0;
        public long volumn = 0;
        public String volumnSuffix;
    }

}
