package com.dianping.cache.alarm.event.alarmDelayCache;

import com.dianping.cache.alarm.entity.AlarmDetail;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lvshiyun on 15/12/26.
 */
public class EventCache {

    private Map<String, DelayItem> eventCache = new HashMap<String, DelayItem>();

    private static final EventCache INSTANCE = new EventCache();

    public static EventCache getInstance() {
        return INSTANCE;
    }


    public synchronized void put(AlarmDetail alarmDetail) {
        DelayItem delayItem = new DelayItem(alarmDetail);

        if (null == eventCache.get(delayItem.getDetail())) {
            delayItem.setCount(1);
            delayItem.setTimeStamp(new Date());
            eventCache.put(delayItem.getDetail(), delayItem);
        } else {
            delayItem = eventCache.get(alarmDetail.getAlarmDetail());
            delayItem.setCount(delayItem.getCount() + 1);
            delayItem.setTimeStamp(new Date());
            eventCache.put(delayItem.getDetail(), delayItem);
        }
    }

    public synchronized boolean check(AlarmDetail alarmDetail) {
        DelayItem delayItem = eventCache.get(alarmDetail.getAlarmDetail());

        if ((1 == delayItem.getCount()) || (10 == delayItem.getCount()) || (30 == delayItem.getCount()) || (60 == delayItem.getCount()) || (120 == delayItem.getCount()) || (240 == delayItem.getCount())) {
            return true;
        } else if ((240 <= delayItem.getCount()) && (0 == delayItem.getCount() % 240)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void flush(){
        List<String> toDelete = new ArrayList<String>();
        for(Map.Entry<String, DelayItem> entry: eventCache.entrySet()){
            DelayItem delayItem = entry.getValue();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(delayItem.getTimeStamp());
            //如果2分钟中内没有新的告警，则删除缓存
            gc.add(12, 2);

            Date timeStamp = gc.getTime();

            Date now = new Date();
            if(now.after(timeStamp)){
                toDelete.add(entry.getKey());
            }
        }

        for(String todelete : toDelete){
            eventCache.remove(todelete);
        }
    }

}
