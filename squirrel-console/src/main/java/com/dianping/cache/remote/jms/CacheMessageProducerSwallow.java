/**
 * Project: com.dianping.cache-server-2.0.1-old
 * 
 * File Created at 2011-10-14
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
package com.dianping.cache.remote.jms;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.dianping.cache.remote.jms.convert.Object2BytesConverter;
import com.dianping.lion.client.ConfigCache;
import com.dianping.pigeon.threadpool.NamedThreadFactory;
import com.dianping.swallow.Destination;
import com.dianping.swallow.MQService;
import com.dianping.swallow.MessageProducer;
import com.dianping.swallow.impl.MongoMQService;

/**
 * CacheMessageProducerSwallow
 * @author youngphy.yang
 */
public class CacheMessageProducerSwallow implements Serializable, InitializingBean, MQSender{

	private static final long serialVersionUID = 6415095593730417120L;

	private Logger logger = LoggerFactory.getLogger(CacheMessageProducerSwallow.class);
	
	private static final String CONFIG_KEY_SWALLOW_ENABLED = "avatar-cache.swallow.enabled";
    
    private String mongoUri = null;
    private Object2BytesConverter object2BytesConverter = null;
    private String destination = null;
    private String type = null;
    private MessageProducer messageProducer = null;
    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            1, 1, 20L, TimeUnit.SECONDS, 
            new ArrayBlockingQueue<Runnable>(10000), 
            new NamedThreadFactory("cache-swallow-send", true), 
            new RejectedExecutionHandler() {
                int count = 0;
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    if(++count < 0) count = 0;
                    if(count % 100 == 0)
                        logger.error("swallow send queue full, discarded " + count + " messages");
                }
            });

    /**
     * Send message to queue.
     * 
     * @param msg
     *            message used to send to queue
     */
    public void sendMessageToTopic(final Object msg) {
    	if (isSendSwallowRequired()) {
    	    threadPool.submit(new Runnable() {
    	        public void run() {
    	            long start = System.currentTimeMillis();
    	            try {
    	                byte[] bytes = object2BytesConverter.convertObject2Bytes(msg);
    	                messageProducer.send(messageProducer.createBinaryMessage(bytes));
    	            } catch (Exception e) {
    	                logger.error("failed to send message to swallow", e);
    	            }
    	            long span = System.currentTimeMillis() - start;
    	            if(span > 100) {
    	                logger.warn("sendMessageToSwallow took " + span);
    	            }
    	        }
    	    });
    	}
    }
    
    private boolean isSendSwallowRequired() {
    	Boolean amqEnabled = null;
    	try {
			amqEnabled = ConfigCache.getInstance().getBooleanProperty(CONFIG_KEY_SWALLOW_ENABLED);
		} catch (Throwable e) {
			logger.warn("Get config[" + CONFIG_KEY_SWALLOW_ENABLED + "] from lion failed.", e);
		}
		return amqEnabled != null ? amqEnabled : true;
	}

	public void setMongoUri(String mongoUri) {
		this.mongoUri = mongoUri;
	}

	public void setObject2BytesConverter(
			Object2BytesConverter object2BytesConverter) {
		this.object2BytesConverter = object2BytesConverter;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Destination dest = null;
		MQService sqs = new MongoMQService(mongoUri);
		if("topic".equals(type)) {
			dest = Destination.topic(destination);
		} else {
			dest = Destination.queue(destination);
		}
		messageProducer = sqs.createProducer(dest, null);
	}
    
}