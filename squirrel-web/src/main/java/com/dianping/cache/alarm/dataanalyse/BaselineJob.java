package com.dianping.cache.alarm.dataanalyse;


import com.dianping.cache.alarm.threadmanager.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineJob")
public class BaselineJob {
    protected static Logger logger = LoggerFactory.getLogger(BaselineJob.class);

    @Autowired
    BaselineThreadFactory baselineThreadFactory;


    @Scheduled(cron = "0 30 1 ? * SUN")//每个周日的1点30触发定时任务
    public void baselineWeeklyJob() {
        logger.info("baselineWeeklyJob", getClass().getSimpleName());

        BaselineThread baselineThread = baselineThreadFactory.createBaselineThread();

        ThreadManager.getInstance().execute(baselineThread);

    }

}
