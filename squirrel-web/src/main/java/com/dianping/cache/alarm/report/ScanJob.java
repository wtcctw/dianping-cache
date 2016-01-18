package com.dianping.cache.alarm.report;


import com.dianping.cache.alarm.report.thread.ScanThread;
import com.dianping.cache.alarm.report.thread.ScanThreadFactory;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("scanJob")
public class ScanJob {
    protected static Logger logger = LoggerFactory.getLogger(ScanJob.class);

    @Autowired
    ScanThreadFactory scanThreadFactory;


    @Scheduled(cron = "0 0 1 * * ?")//每天的1点触发定时任务
//    @Scheduled(cron = "0 */1 * * * ?")
    public void baselineWeeklyJob() throws InterruptedException, DocumentException, URISyntaxException {

        logger.info("scanTaskDailyJob", getClass().getSimpleName());

        ScanThread scanThread = scanThreadFactory.createScanThread();
//
        ThreadManager.getInstance().execute(scanThread);

    }

}
