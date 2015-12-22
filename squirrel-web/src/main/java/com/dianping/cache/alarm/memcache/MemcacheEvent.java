package com.dianping.cache.alarm.memcache;


import com.dianping.cache.alarm.entity.AlarmConfig;
import com.dianping.cache.alarm.entity.AlarmDetail;
import com.dianping.cache.alarm.event.Event;
import com.dianping.cache.alarm.receiver.ReceiverService;
import org.dom4j.DocumentException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by lvshiyun on 15/11/27.
 */
@Component("memcacheEvent")
@Scope("prototype")
public class MemcacheEvent extends Event {

    BlockingQueue<AlarmDetail> AlarmConfigQueue;

    private int AlarmConfigQueueSize = 5000;

    public MemcacheEvent(){
        super();
        AlarmConfigQueue = new ArrayBlockingQueue<AlarmDetail>(AlarmConfigQueueSize);
    }

    public void put(AlarmDetail alarmDetail){
        AlarmConfigQueue.add(alarmDetail);
    }


    @Override
    public void alarm() throws InterruptedException, URISyntaxException, DocumentException {

        while (!AlarmConfigQueue.isEmpty()){
            sendMessage(AlarmConfigQueue.take());
        }
    }


    @Override
    @Resource
    public void setReceiverService(ReceiverService receiverService) {
        this.receiverService = receiverService;
    }
}
