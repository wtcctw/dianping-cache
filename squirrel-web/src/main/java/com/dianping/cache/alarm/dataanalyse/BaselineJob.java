package com.dianping.cache.alarm.dataanalyse;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 15/12/31.
 */
@Component("baselineJob")
public class BaselineJob {
        @Scheduled(cron = "0 30 2 ? * SUN")//每个周日的2点30触发定时任务
        public void baselineWeeklyJob(){
            System.out.println("baseline job......");
        }

}
