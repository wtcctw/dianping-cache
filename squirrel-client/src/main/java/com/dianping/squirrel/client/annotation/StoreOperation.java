/**
 * Project: squirrel
 * 
 * File Created at 2010-8-10
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
package com.dianping.squirrel.client.annotation;

/**
 * Cache operation
 */
public enum StoreOperation {
	/**
	 * Set and get
	 */
	SetAndGet,
	/**
	 * Remove cache
	 */
	Remove,
	/**
	 * Update cache
	 */
	Update;
}
