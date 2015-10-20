/**
 * Project: avatar
 * 
 * File Created at 2010-7-14
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
package com.dianping.avatar.cache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Cache key object
 * 
 * @author danson.liu
 * 
 */
public class CacheKey implements Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -1099870460150967658L;

	/**
	 * Item category
	 */
	private String category;

	/**
	 * Parameters
	 */
	private Object[] params;

	/**
	 * Constructor
	 */
	public CacheKey(String category, Object... params) {
		this.category = category;
		this.params = params;
	}

	public String getCategory() {
		return category;
	}

	/**
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	public List<Object> getParamsAsList() {
		if (params == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(params);
	}

	@Override
	public String toString() {
		return "CacheKey[category:" + category + ", indexParams:" + ArrayUtils.toString(params) + "]";
	}
	
	public int hashCode() {
	    return new HashCodeBuilder(17, 37).
	            append(category).
	            append(params).
	            toHashCode();
	}

	public boolean equals(Object obj) {
	    if (obj == null) { return false; }
	    if (obj == this) { return true; }
	    if (obj.getClass() != getClass()) {
	        return false;
	    }
	    CacheKey ck = (CacheKey) obj;
	    return new EqualsBuilder().
	            append(category, ck.category).
	            append(params, ck.params).
	            isEquals();
	}
	
}
