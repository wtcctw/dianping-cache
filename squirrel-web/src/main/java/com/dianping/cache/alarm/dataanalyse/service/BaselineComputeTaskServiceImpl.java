package com.dianping.cache.alarm.dataanalyse.service;

import com.dianping.cache.alarm.dao.BaselineComputeTaskDao;
import com.dianping.cache.alarm.entity.BaselinecomputeTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lvshiyun on 16/1/4.
 */
@Component
public class BaselineComputeTaskServiceImpl implements BaselineComputeTaskService {
    @Autowired
    BaselineComputeTaskDao baselineComputeTaskDao;

    @Override
    public void insert(BaselinecomputeTask baselinecomputeTask) {
        baselineComputeTaskDao.insert(baselinecomputeTask);
    }

    @Override
    public List<BaselinecomputeTask> getRecentTaskId() {
        return baselineComputeTaskDao.getRecentTaskId();
    }
}
