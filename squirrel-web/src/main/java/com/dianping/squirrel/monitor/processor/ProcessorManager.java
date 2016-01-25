package com.dianping.squirrel.monitor.processor;

import com.dianping.squirrel.common.config.ConfigManager;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.util.NamedThreadFactory;
import com.dianping.squirrel.monitor.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ProcessorManager {

    private static Logger logger = LoggerFactory.getLogger(ProcessorManager.class);
    
    private static final String KEY_USE_SHARED_POOL = "squirrel.monitor.processor.useSharedPool";
    private static final boolean DEFAULT_USE_SHARED_POOL = true;
    private static final String KEY_CORE_SIZE = "squirrel.monitor.processor.coreSize";
    private static final int DEFAULT_CORE_SIZE = 20;
    private static final String KEY_MAX_SIZE = "squirrel.monitor.processor.maxSize";
    private static final int DEFAULT_MAX_SIZE = 20;
    private static final String KEY_QUEUE_LENGTH = "squirrel.monitor.processor.queueLength";
    private static final int DEFAULT_QUEUE_LENGTH = 10000;
    
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    
    private boolean useSharedPool;
    private int coreSize;
    private int maxSize;
    private int queueLength;
    
    private ExecutorService sharedExecutor;
    
    private Map<String, ProcessorChain> processorMap;
    
    public ProcessorManager() {
        useSharedPool = configManager.getBooleanValue(KEY_USE_SHARED_POOL, DEFAULT_USE_SHARED_POOL);
        coreSize = configManager.getIntValue(KEY_CORE_SIZE, DEFAULT_CORE_SIZE);
        maxSize = configManager.getIntValue(KEY_MAX_SIZE, DEFAULT_MAX_SIZE);
        queueLength = configManager.getIntValue(KEY_QUEUE_LENGTH, DEFAULT_QUEUE_LENGTH);
        if(useSharedPool) {
            sharedExecutor = new ThreadPoolExecutor(coreSize, maxSize, 30, TimeUnit.SECONDS, 
                    new ArrayBlockingQueue(queueLength), 
                    new NamedThreadFactory("squirrel-monitor-processor-share", true),
                    new CallerRunsPolicy());
        }
        processorMap = new HashMap<String, ProcessorChain>();
    }
    
    public void registerProcessor(String dataType, Processor processor) {
        ExecutorService executor = useSharedPool ? sharedExecutor : Executors.newSingleThreadExecutor(new NamedThreadFactory("squirrel-processor-" + dataType, true));
        registerProcessor(dataType, processor, executor);
    }
    
    public synchronized void registerProcessor(String dataType, Processor processor, ExecutorService executor) {
        ProcessorChain processorChain = processorMap.get(dataType);
        if(processorChain == null) {
            processorChain = new ProcessorChain(dataType);
            processorMap.put(dataType, processorChain);
        }
        processorChain.addProcessor(processor, executor);
    }
    
    public void process(Data data) {
        checkNotNull(data, "data is null");
        ProcessorChain processorChain = processorMap.get(data.getType().toString());
        if(processorChain != null) {
            processorChain.process(data);
        } else {
            logger.warn("no processor for " + data.getType());
        }
    }
    
}
