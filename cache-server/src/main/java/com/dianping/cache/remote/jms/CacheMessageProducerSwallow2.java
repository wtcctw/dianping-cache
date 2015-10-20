/**
 * Copyright 2014 dianping.com.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.dianping.lion.client.ConfigCache;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.producer.Producer;
import com.dianping.swallow.producer.ProducerFactory;
import com.dianping.swallow.producer.impl.ProducerFactoryImpl;

/**
 * CacheMessageProducerSwallow2
 * 
 * @author enlight.chen
 */
public class CacheMessageProducerSwallow2 implements Serializable, InitializingBean, MQSender{

    private static final long serialVersionUID = 1L;

    private static final String CONFIG_KEY_SWALLOW2_ENABLED = "avatar-cache.swallow2.enabled";
    
    private Logger logger = LoggerFactory.getLogger(CacheMessageProducerSwallow2.class);
    
    private String destination = null;
    private Producer messageProducer = null;

    /**
     * Send message to topic.
     * 
     * @param msg
     *            message used to send to topic
     */
    public void sendMessageToTopic(final Object msg) {
    	if (isSendSwallowRequired()) {
    	    long start = System.currentTimeMillis();
	    	try {
	    		messageProducer.sendMessage(msg, msg.getClass().getName());
	    	} catch (Exception e) {
	    		logger.error("failed to send message to destination " + destination, e);
	    		throw new RuntimeException(e);
	    	}
	    	long span = System.currentTimeMillis() - start;
            if(span > 100) {
                logger.warn("sendMessageToSwallow2 took " + span);
            }
    	}
    }
    
    private boolean isSendSwallowRequired() {
    	Boolean enabled = null;
    	try {
			enabled = ConfigCache.getInstance().getBooleanProperty(CONFIG_KEY_SWALLOW2_ENABLED);
		} catch (Throwable e) {
			logger.warn("failed to get config " + CONFIG_KEY_SWALLOW2_ENABLED + " from lion", e);
		}
		return enabled != null ? enabled : true;
	}


	public void setDestination(String destination) {
		this.destination = destination;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	    ProducerFactory producerFactory = ProducerFactoryImpl.getInstance();
	    Destination dest = Destination.topic(destination);
	    messageProducer = producerFactory.createProducer(dest);
	}
    
}