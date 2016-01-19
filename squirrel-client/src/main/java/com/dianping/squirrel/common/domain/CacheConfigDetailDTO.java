/**
 * Project: avatar-cache-remote
 * 
 * File Created at 2010-10-18
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.squirrel.common.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * CacheConfigDetailDTO
 * 
 * @author danson.liu
 * 
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
@JsonSubTypes({ @JsonSubTypes.Type(value = EhcacheConfigDetailDTO.class, name = "EhcacheConfigDetailDTO"),
		@JsonSubTypes.Type(value = MemcachedConfigDetailDTO.class, name = "MemcachedConfigDetailDTO"),
		@JsonSubTypes.Type(value = DcacheConfigDetailDTO.class, name = "DcacheConfigDetailDTO"),
		@JsonSubTypes.Type(value = DangaConfigDetailDTO.class, name = "DangaConfigDetailDTO")  })
public abstract class CacheConfigDetailDTO extends AbstractDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 463815040207539685L;

	protected String clientClazz;
	
	protected String className;

	public String getClientClazz() {
		return clientClazz;
	}

	public void setClientClazz(String clientClazz) {
		this.clientClazz = clientClazz;
	}

}
