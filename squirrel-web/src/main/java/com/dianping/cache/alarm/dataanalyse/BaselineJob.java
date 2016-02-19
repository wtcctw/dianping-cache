package com.dianping.cache.alarm.dataanalyse;


import com.dianping.cache.alarm.dataanalyse.thread.BaselineCleanThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineComputeThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineMapGetThread;
import com.dianping.cache.alarm.dataanalyse.thread.BaselineThreadFactory;
import com.dianping.cache.alarm.threadmanager.ThreadManager;
import com.dianping.cache.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineJob")
public class BaselineJob {
    protected static Logger logger = LoggerFactory.getLogger(BaselineJob.class);

    private static final ArrayList<String> IPLIST = new ArrayList<String>() {{
        add("10.1.14.104");//线上
        add("10.2.7.129");//ppe
        add("192.168.227.113");//beta
        add("172.24.121.42");//my host
    }};


    @Autowired
    BaselineThreadFactory baselineThreadFactory;


    @Scheduled(cron = "0 30 1 ? * SUN")//每个周日的1点30触发定时任务
    public void baselineWeeklyJob() {
        if (isMaster()) {
            logger.info("baselineWeeklyJob", getClass().getSimpleName());

            BaselineComputeThread baselineComputeThread = baselineThreadFactory.createBaselineComputeThread();

            ThreadManager.getInstance().execute(baselineComputeThread);
        }
    }

    @Scheduled(cron = "0 0 3 ? * SUN")//每周触发一次历史记录清理任务
    public void baselineCleanTask() {
        if (isMaster()) {
            logger.info("baseline clean job", getClass().getSimpleName());

            BaselineCleanThread baselineCleanThread = baselineThreadFactory.createBaselineCleanThread();

            ThreadManager.getInstance().execute(baselineCleanThread);
        }
    }



    @Scheduled(cron = "0 10 * * * ?")//每小时10分触发一次
    public void baselineMapGetTask() {
        if (isMaster()) {
            logger.info("baselineMapGet job,Time:"+(new Date()).toString(), getClass().getSimpleName());

            BaselineMapGetThread baselineMapGetThread = baselineThreadFactory.createBaselineMapGetThread();

            ThreadManager.getInstance().execute(baselineMapGetThread);
        }
    }



    public boolean isMaster() {
        boolean isMaster = false;
        try {
            List<String> ip = NetUtil.getAllLocalIp();
            ip.retainAll(IPLIST);
            if (ip.size() > 0)
                isMaster = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isMaster;
    }
}
