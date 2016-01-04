package com.dianping.cache.alarm.dataanalyse;

import org.springframework.stereotype.Service;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Service
public class BaselineServiceImpl implements BaselineService {
    @Override
    public Baseline getBaseline(String name) {

        return BaselineDict.getInstance().getBaseline(name);

    }

    @Override
    public void putBaseline(String name, Baseline baseline) {

        BaselineDict.getInstance().putBaseline(name, baseline);

    }
}
