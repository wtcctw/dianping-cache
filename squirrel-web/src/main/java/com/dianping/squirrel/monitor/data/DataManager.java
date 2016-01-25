package com.dianping.squirrel.monitor.data;

import com.dianping.squirrel.monitor.processor.ProcessorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataManager {

    @Autowired
    private ProcessorManager processorManager;
    
    public void addData(Data data) {
        if(data != null)
            processorManager.process(data);
    }

}
