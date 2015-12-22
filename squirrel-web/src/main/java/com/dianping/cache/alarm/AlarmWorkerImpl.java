package com.dianping.cache.alarm;

import com.dianping.cache.alarm.event.Event;
import com.dianping.cache.alarm.event.EventChannel;
import com.dianping.cache.alarm.lifecycle.AbstractLifeCycle;
import com.dianping.cache.alarm.lifecycle.AlarmerLifecycle;
import com.dianping.cache.alarm.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by lvshiyun on 15/11/23.
 */
@Component
public class AlarmWorkerImpl extends AbstractLifeCycle implements AlarmerLifecycle, AlarmWorker {

    @Autowired
    private EventChannel eventChannel;

    private volatile boolean isStopped = false;

    @Autowired
    protected AlarmerTaskManager alarmerTaskManager;

    private Thread alarmTaskThread;

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        isStopped = false;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        alarmTaskThread = ThreadUtils.createThread(new Runnable() {
            @Override
            public void run() {
                startAlarmer();
            }
        }, "AlarmWoker-Cache", true);
        alarmTaskThread.start();
    }

    private boolean checkStop() {
        return isStopped || Thread.currentThread().isInterrupted();
    }

    @Override
    public void startAlarmer() {
        while (!checkStop()) {
            Event event = null;
            try {
                event = eventChannel.next();
                logger.info("[start] {}.", event.toString());
                alarmerTaskManager.submit(new AlarmTask(event));
            } catch (InterruptedException e) {
                e.printStackTrace();
                try {
                    TimeUnit.SECONDS.sleep(200);
                } catch (InterruptedException e1) {

                }
                logger.error("[start] lost event {}.", event.toString());
            }
        }

    }

    @Override
    protected void doStop() throws Exception {
        stopAlarmer();
    }

    @Override
    protected void doDispose() throws Exception {
        super.doDispose();
    }

    @Override
    public void stopAlarmer() {
        isStopped = true;
        alarmTaskThread.interrupt();
    }


    private class AlarmTask implements Runnable {
        private Event event;

        private AlarmTask(Event event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                event.alarm();
                logger.info("[run] {}.", event.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("[run] alarm event failed.", e);
            }
        }
    }
}
