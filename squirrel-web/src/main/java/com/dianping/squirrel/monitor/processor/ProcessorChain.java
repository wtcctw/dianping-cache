package com.dianping.squirrel.monitor.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.dianping.squirrel.monitor.data.Data;

public class ProcessorChain implements Processor {
    
    private String dataType;
    private Map<Processor, ExecutorService> processors;
    
    public ProcessorChain(String dataType) {
        this.dataType = dataType;
        processors = new HashMap<Processor, ExecutorService>();
    }
    
    @Override
    public void process(Data data) {
        for(Map.Entry<Processor, ExecutorService> entry : processors.entrySet()) {
            Processor processor = entry.getKey();
            ExecutorService executor = entry.getValue();
            executor.execute(new RunnableProcessor(processor, data));
        }
    }
    
    public void addProcessor(Processor processor, ExecutorService executor) {
        if(processor != null && executor != null) {
            processors.put(processor, executor);
        }
    }
    
}
