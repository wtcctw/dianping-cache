package com.dianping.squirrel.monitor.data;

import com.dianping.squirrel.monitor.processor.ProcessorManager;

public class DataManager {
    
    private ProcessorManager processorManager;
    
    public void addData(Data data) {
        processorManager.process(data);
    }
    
}
