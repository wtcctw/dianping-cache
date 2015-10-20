/**
 * Project: com.dianping.cache-core-2.0.0
 * 
 * File Created at 2011-4-19
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.cache.memcached;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ehcache.util.NamedThreadFactory;
import net.spy.memcached.KetamaConnectionFactory;
import net.spy.memcached.transcoders.Transcoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cache.config.ConfigManagerLoader;

/**
 * @author jian.liu
 *
 */
public class ExtendedKetamaConnectionFactory extends KetamaConnectionFactory implements ExtendedConnectionFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ExtendedKetamaConnectionFactory.class);
    
    // default operation timeout in milliseconds
    private static final long DEFAULT_OPERATION_TIMEOUT = 50;
    
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    
	private Transcoder<Object> transcoder;
	private long opQueueMaxBlockTime;
	private long operationTimeout;
	
    private int coreSize = ConfigManagerLoader.getConfigManager().getIntValue("avatar-cache.future.coresize", CORES);
    private int maxSize = ConfigManagerLoader.getConfigManager().getIntValue("avatar-cache.future.maxsize", 5*CORES);
    private int queueSize = ConfigManagerLoader.getConfigManager().getIntValue("avatar-cache.future.queuesize", 10000);
    private ExecutorService executorService;
    
	public ExtendedKetamaConnectionFactory(int qLen, int bufSize, long opQueueMaxBlockTime) {
		this(qLen, bufSize, opQueueMaxBlockTime, DEFAULT_OPERATION_TIMEOUT);
	}
	
	public ExtendedKetamaConnectionFactory(int qLen, int bufSize, long opQueueMaxBlockTime, long operationTimeout) {
	    super(qLen, bufSize, opQueueMaxBlockTime);
	    this.opQueueMaxBlockTime = opQueueMaxBlockTime;
	    this.operationTimeout = operationTimeout;
	}
	
	@Override
	public long getOpQueueMaxBlockTime() {
	    return opQueueMaxBlockTime;
	}
	
	@Override
	public long getOperationTimeout() {
	    return operationTimeout;
	}
	
	public ExtendedKetamaConnectionFactory() {
		this(DEFAULT_OP_QUEUE_LEN, DEFAULT_READ_BUFFER_SIZE, DEFAULT_OP_QUEUE_MAX_BLOCK_TIME);
	}
	
	@Override
	public Transcoder<Object> getDefaultTranscoder() {
		return transcoder;
	}

	public void setTranscoder(Transcoder<Object> transcoder) {
		this.transcoder = transcoder;
	}
	
	@Override
	public ExecutorService getListenerExecutorService() {
	    logger.info(">>>>>>getListenerExecutorService()");
	    if(executorService == null) {
	        executorService = new ThreadPoolExecutor(coreSize, maxSize, 30L, TimeUnit.SECONDS, 
	            new ArrayBlockingQueue<Runnable>(queueSize), 
	            new NamedThreadFactory("cache-future-listener"), 
	            new RejectedExecutionHandler() {
                    AtomicInteger count = new AtomicInteger();
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        if(count.getAndIncrement() % 100 == 0) {
                            logger.error("cache future listener overflow!!!");
                        }
                    }
                    
                });
	    }
	    return executorService;
	}
	
}
