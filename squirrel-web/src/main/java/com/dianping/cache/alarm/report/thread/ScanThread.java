package com.dianping.cache.alarm.report.thread;

import com.dianping.cache.alarm.report.task.ScanTask;
import com.dianping.cache.alarm.report.task.ScanTaskFactory;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.net.URISyntaxException;

/**
 * Created by lvshiyun on 16/1/7.
 */
@Component("scanThread")
@Scope("prototype")
public class ScanThread implements Runnable{

    @Autowired
    ScanTaskFactory scanTaskFactory;

    @Override
    public void run() {
        ScanTask scanTask = scanTaskFactory.createScanTask();
        try {
            try {
                scanTask.run();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
