package com.dianping.cache.alarm.report;

import com.dianping.cache.alarm.event.Event;
import com.dianping.cache.alarm.event.EventChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by lvshiyun on 15/11/27.
 */
@Component
public class EventReporterImpl implements EventReporter {

    private static final Logger logger = LoggerFactory.getLogger(EventReporterImpl.class);

    @Autowired
    private EventChannel eventChannel;

    public EventReporterImpl(){

    }

    public EventReporterImpl(EventChannel eventChannel){
        this.eventChannel = eventChannel;
    }

    @Override
    public void report(Event event) {

        try {
            eventChannel.put(event);
        }   catch (InterruptedException e) {
            logger.error("[report]", e);
        }
    }

    public EventChannel getEventChannel() {
        return eventChannel;
    }

    public void setEventChannel(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
    }
}
