package com.dianping.squirrel.monitor.processor;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.monitor.data.Data;

public class RunnableProcessor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RunnableProcessor.class);
    
    private Processor processor;
    private Data data;
    
    public RunnableProcessor(Processor processor, Data data) {
        checkNotNull(processor, "processor is null");
        checkNotNull(data, "data is null");
        this.processor = processor;
        this.data = data;
    }

    @Override
    public void run() {
        try {
            processor.process(data);
        } catch(Throwable t) {
            logger.error("", t);
        }
    }

}
