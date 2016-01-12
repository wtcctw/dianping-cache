package com.dianping.cache.alarm.dataanalyse;

import com.dianping.cache.alarm.AlarmerTaskManager;
import com.dianping.cache.alarm.dataanalyse.baselineCache.BaselineCacheService;
import com.dianping.cache.alarm.lifecycle.AbstractLifeCycle;
import com.dianping.cache.alarm.lifecycle.AlarmerLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lvshiyun on 15/11/21.
 */
@Service
public class DataAnalyser extends AbstractLifeCycle implements AlarmerLifecycle {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String DODATAANALYSER_FUNCTION = "-doDataAnalyser";

    @Autowired
    BaselineCacheService baselineCacheService;


    @Autowired
    protected AlarmerTaskManager alarmerTaskManager;

    protected String dataAnalyserName;

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        dataAnalyserName = getClass()+ DODATAANALYSER_FUNCTION;

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

//        baselineCacheService.reload();

        logger.info("[doStart] {} start."+ getClass().getSimpleName());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        logger.info("[doStop] {} stop."+getClass().getSimpleName());
    }
}
