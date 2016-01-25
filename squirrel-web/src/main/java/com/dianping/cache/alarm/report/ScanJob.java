package com.dianping.cache.alarm.report;


import com.dianping.cache.alarm.report.thread.ScanThread;
import com.dianping.cache.alarm.report.thread.ScanThreadFactory;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import com.dianping.cache.util.NetUtil;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("scanJob")
public class ScanJob {
    protected static Logger logger = LoggerFactory.getLogger(ScanJob.class);

    private static final ArrayList<String> IPLIST = new ArrayList<String>(){{
        add("10.1.14.104");//线上
        add("10.2.7.129");//ppe
        add("192.168.227.113");//beta
        add("172.24.121.42");//my host
    }};

    @Autowired
    ScanThreadFactory scanThreadFactory;


    @Scheduled(cron = "0 42 10 * * ?")//每天的1点触发定时任务
//    @Scheduled(cron = "0 */1 * * * ?")
    public void baselineWeeklyJob() throws InterruptedException, DocumentException, URISyntaxException {

        logger.info("scanTaskDailyJob", getClass().getSimpleName());

        if(isMaster()) {
            ScanThread scanThread = scanThreadFactory.createScanThread();
            ThreadManager.getInstance().execute(scanThread);
        }
    }


    public boolean isMaster(){
        boolean isMaster = false;
        try {
            List<String> ip= NetUtil.getAllLocalIp();
            ip.retainAll(IPLIST);
            if(ip.size() > 0)
                isMaster = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isMaster;
    }

}
