package com.dianping.cache.alarm.memcache;

import com.dianping.cache.alarm.AbstractAlarmer;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lvshiyun on 15/11/30.
 */
public abstract class AbstractMemcacheAlarmer extends AbstractAlarmer {

    private ScheduledFuture<?> future;

    protected int alarmInterval = 30;
    protected int alarmDelay = 30;

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        startAlarm();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    @Override
    protected void doDispose() throws Exception {

    }

    public abstract void doAlarm() throws InterruptedException, MemcachedException, IOException, TimeoutException;

    public void startAlarm() {
        future = alarmerTaskManager.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    doAlarm();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (MemcachedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, getAlarmDelay(), getAlarmInterval(), TimeUnit.SECONDS);
    }

    public int getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(int alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public int getAlarmDelay() {
        return alarmDelay;
    }

    public void setAlarmDelay(int alarmDelay) {
        this.alarmDelay = alarmDelay;
    }
}
