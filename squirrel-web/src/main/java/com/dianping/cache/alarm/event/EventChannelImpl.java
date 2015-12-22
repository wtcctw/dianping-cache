package com.dianping.cache.alarm.event;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by lvshiyun on 15/11/23.
 */
@Component
public class EventChannelImpl implements EventChannel {

    private BlockingQueue<Event> eventBuffer;

    private int bufferSize = 5000;

    public EventChannelImpl() {
        eventBuffer = new ArrayBlockingQueue<Event>(bufferSize);
    }

    public EventChannelImpl(int bufferSize) {
        this.bufferSize = bufferSize;
        eventBuffer = new ArrayBlockingQueue<Event>(bufferSize);
    }

    @Override
    public void put(Event event) throws InterruptedException {
        eventBuffer.put(event);
    }

    @Override
    public Event next() throws InterruptedException {
        return eventBuffer.take();
    }
}
