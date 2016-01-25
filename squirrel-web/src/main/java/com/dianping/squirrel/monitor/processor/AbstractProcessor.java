package com.dianping.squirrel.monitor.processor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * hui.wang@dianping.com
 * Created by hui.wang on 16/1/20.
 */
public abstract class AbstractProcessor implements Processor,InitializingBean{

    @Autowired
    ProcessorManager processorManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        for(String type : getType()){
            processorManager.registerProcessor(type,this);
        }
    }

    public abstract List<String> getType();

}
