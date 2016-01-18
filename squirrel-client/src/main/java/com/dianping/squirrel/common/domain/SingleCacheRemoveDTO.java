/**
 * Project: avatar-cache-remote
 * 
 * File Created at 2010-10-19
 * $Id$
 * 
 * Copyright 2010 dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.squirrel.common.domain;


/**
 * Local cache removing message.
 * 
 * @author pengshan.zhang
 * 
 */
public class SingleCacheRemoveDTO extends CacheMessageDTO {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 6746249464661690655L;
    
    private String cacheType;
    
    /**
     * Key type
     */
    private String cacheKey;

    /**
     * @param msgValue the msgValue to set
     */
    public void setCacheKey(String msgValue) {
        this.cacheKey = msgValue;
    }

    /**
     * @return the msgValue
     */
    public String getCacheKey() {
        return cacheKey;
    }

	/**
	 * @return the cacheType
	 */
	public String getCacheType() {
		return cacheType;
	}

	/**
	 * @param cacheType the cacheType to set
	 */
	public void setCacheType(String cacheType) {
		this.cacheType = cacheType;
	}

}
